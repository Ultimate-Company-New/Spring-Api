package com.example.SpringApi.Helpers;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Helper class for calculating optimal package allocation based on product dimensions.
 * Uses a bin-packing algorithm (First Fit Decreasing) to minimize packaging costs.
 */
@Component
public class PackagingHelper {

    /**
     * Product dimension info for packaging calculation
     */
    public static class ProductDimension {
        private final BigDecimal length;
        private final BigDecimal breadth;
        private final BigDecimal height;
        private final BigDecimal weightKgs;
        private final int quantity;

        public ProductDimension(BigDecimal length, BigDecimal breadth, BigDecimal height, BigDecimal weightKgs, int quantity) {
            this.length = length != null ? length : BigDecimal.ZERO;
            this.breadth = breadth != null ? breadth : BigDecimal.ZERO;
            this.height = height != null ? height : BigDecimal.ZERO;
            this.weightKgs = weightKgs != null ? weightKgs : BigDecimal.ZERO;
            this.quantity = quantity;
        }

        public double getVolume() {
            return length.doubleValue() * breadth.doubleValue() * height.doubleValue();
        }

        public double getWeight() {
            return weightKgs.doubleValue();
        }

        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * Package dimension info for packaging calculation
     */
    public static class PackageDimension {
        private final Long packageId;
        private final String packageName;
        private final String packageType;
        private final int length;
        private final int breadth;
        private final int height;
        private final BigDecimal maxWeight;
        private final BigDecimal pricePerUnit;
        private int availableQuantity;

        public PackageDimension(Long packageId, String packageName, String packageType,
                                int length, int breadth, int height,
                                BigDecimal maxWeight, BigDecimal pricePerUnit, int availableQuantity) {
            this.packageId = packageId;
            this.packageName = packageName;
            this.packageType = packageType;
            this.length = length;
            this.breadth = breadth;
            this.height = height;
            this.maxWeight = maxWeight != null ? maxWeight : BigDecimal.ZERO;
            this.pricePerUnit = pricePerUnit != null ? pricePerUnit : BigDecimal.ZERO;
            this.availableQuantity = availableQuantity;
        }

        public double getVolume() {
            return (double) length * breadth * height;
        }

        public double getMaxWeight() {
            return maxWeight.doubleValue();
        }

        public BigDecimal getPricePerUnit() {
            return pricePerUnit;
        }

        public Long getPackageId() {
            return packageId;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getPackageType() {
            return packageType;
        }

        public int getAvailableQuantity() {
            return availableQuantity;
        }

        public void decrementQuantity() {
            this.availableQuantity--;
        }
    }

    /**
     * Result of packaging calculation - how many of each package type are used
     */
    public static class PackageUsageResult {
        private final Long packageId;
        private final String packageName;
        private final String packageType;
        private final int quantityUsed;
        private final BigDecimal pricePerUnit;
        private final BigDecimal totalCost;

        public PackageUsageResult(Long packageId, String packageName, String packageType,
                                  int quantityUsed, BigDecimal pricePerUnit) {
            this.packageId = packageId;
            this.packageName = packageName;
            this.packageType = packageType;
            this.quantityUsed = quantityUsed;
            this.pricePerUnit = pricePerUnit;
            this.totalCost = pricePerUnit.multiply(BigDecimal.valueOf(quantityUsed));
        }

        public Long getPackageId() { return packageId; }
        public String getPackageName() { return packageName; }
        public String getPackageType() { return packageType; }
        public int getQuantityUsed() { return quantityUsed; }
        public BigDecimal getPricePerUnit() { return pricePerUnit; }
        public BigDecimal getTotalCost() { return totalCost; }
    }

    /**
     * Complete packaging estimate result
     */
    public static class PackagingEstimateResult {
        private final List<PackageUsageResult> packagesUsed;
        private final BigDecimal totalPackagingCost;
        private final int totalPackagesUsed;
        private final int maxItemsPackable;
        private final boolean canPackAllItems;
        private final String errorMessage;

        public PackagingEstimateResult(List<PackageUsageResult> packagesUsed, int requestedItems, int packedItems) {
            this.packagesUsed = packagesUsed;
            this.totalPackagesUsed = packagesUsed.stream().mapToInt(PackageUsageResult::getQuantityUsed).sum();
            this.totalPackagingCost = packagesUsed.stream()
                    .map(PackageUsageResult::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.maxItemsPackable = packedItems;
            this.canPackAllItems = packedItems >= requestedItems;
            this.errorMessage = canPackAllItems ? null : "Not enough packages to pack all items. Can only pack " + packedItems + " of " + requestedItems + " items.";
        }

        public List<PackageUsageResult> getPackagesUsed() { return packagesUsed; }
        public BigDecimal getTotalPackagingCost() { return totalPackagingCost; }
        public int getTotalPackagesUsed() { return totalPackagesUsed; }
        public int getMaxItemsPackable() { return maxItemsPackable; }
        public boolean isCanPackAllItems() { return canPackAllItems; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Internal class to track used packages and their remaining volume
     */
    private static class UsedPackage {
        final PackageDimension packageDim;
        double usedVolume;
        double usedWeight;
        UsedPackage(PackageDimension packageDim) {
            this.packageDim = packageDim;
            this.usedVolume = 0;
            this.usedWeight = 0;
        }

        double getRemainingVolume() {
            return packageDim.getVolume() - usedVolume;
        }

        double getRemainingWeight() {
            return packageDim.getMaxWeight() - usedWeight;
        }

        boolean canFit(double productVolume, double productWeight) {
            return getRemainingVolume() >= productVolume && getRemainingWeight() >= productWeight;
        }

        void addProduct(double productVolume, double productWeight) {
            this.usedVolume += productVolume;
            this.usedWeight += productWeight;
        }
    }

    /**
     * Calculate optimal packaging for given product quantity using available packages.
     * Uses First Fit Decreasing bin-packing algorithm optimized for cost.
     *
     * @param product Product dimensions
     * @param availablePackages List of available packages at the location
     * @return PackagingEstimateResult with packages needed and costs
     */
    public PackagingEstimateResult calculatePackaging(ProductDimension product, List<PackageDimension> availablePackages) {
        int requestedItems = product.getQuantity();
        
        if (requestedItems <= 0) {
            return new PackagingEstimateResult(Collections.emptyList(), 0, 0);
        }

        double productVolume = product.getVolume();
        double productWeight = product.getWeight();

        // Create mutable copies of packages sorted by price (cheapest first for cost optimization)
        List<PackageDimension> packages = new ArrayList<>();
        for (PackageDimension pkg : availablePackages) {
            // Create a copy with the available quantity
            packages.add(new PackageDimension(
                    pkg.getPackageId(),
                    pkg.getPackageName(),
                    pkg.getPackageType(),
                    pkg.length,
                    pkg.breadth,
                    pkg.height,
                    pkg.maxWeight,
                    pkg.pricePerUnit,
                    pkg.getAvailableQuantity()
            ));
        }

        // Sort packages by cost-efficiency (price per volume, lowest first)
        packages.sort((a, b) -> {
            double costPerVolumeA = a.getVolume() > 0 ? a.getPricePerUnit().doubleValue() / a.getVolume() : Double.MAX_VALUE;
            double costPerVolumeB = b.getVolume() > 0 ? b.getPricePerUnit().doubleValue() / b.getVolume() : Double.MAX_VALUE;
            return Double.compare(costPerVolumeA, costPerVolumeB);
        });

        // Track used packages
        List<UsedPackage> usedPackages = new ArrayList<>();
        Map<Long, Integer> packageUsageCount = new HashMap<>();
        int packedItems = 0;

        // Try to pack each product
        for (int i = 0; i < requestedItems; i++) {
            boolean packed = false;

            // First, try to fit in an already-used package (sorted by most remaining volume)
            usedPackages.sort((a, b) -> Double.compare(b.getRemainingVolume(), a.getRemainingVolume()));
            
            for (UsedPackage usedPkg : usedPackages) {
                if (usedPkg.canFit(productVolume, productWeight)) {
                    usedPkg.addProduct(productVolume, productWeight);
                    packed = true;
                    packedItems++;
                    break;
                }
            }

            // If not packed, get a new package
            if (!packed) {
                PackageDimension newPackage = getNextSuitablePackage(packages, productVolume, productWeight);
                if (newPackage != null) {
                    newPackage.decrementQuantity();
                    UsedPackage usedPkg = new UsedPackage(newPackage);
                    usedPkg.addProduct(productVolume, productWeight);
                    usedPackages.add(usedPkg);
                    packageUsageCount.merge(newPackage.getPackageId(), 1, Integer::sum);
                    packed = true;
                    packedItems++;
                }
            }

            // If still not packed, we ran out of packages
            if (!packed) {
                break;
            }
        }

        // Build result
        List<PackageUsageResult> results = new ArrayList<>();
        for (PackageDimension pkg : availablePackages) {
            int count = packageUsageCount.getOrDefault(pkg.getPackageId(), 0);
            if (count > 0) {
                results.add(new PackageUsageResult(
                        pkg.getPackageId(),
                        pkg.getPackageName(),
                        pkg.getPackageType(),
                        count,
                        pkg.getPricePerUnit()
                ));
            }
        }

        // Sort results by package type/name for consistent display
        results.sort(Comparator.comparing(PackageUsageResult::getPackageName));

        return new PackagingEstimateResult(results, requestedItems, packedItems);
    }

    /**
     * Find the next suitable package that can fit the product.
     * Prioritizes cost-efficient packages that are still available.
     */
    private PackageDimension getNextSuitablePackage(List<PackageDimension> packages, double productVolume, double productWeight) {
        // First, find smallest package that fits (already sorted by cost-efficiency)
        for (PackageDimension pkg : packages) {
            if (pkg.getAvailableQuantity() > 0 &&
                    pkg.getVolume() >= productVolume &&
                    pkg.getMaxWeight() >= productWeight) {
                return pkg;
            }
        }

        // If no package fits, return the largest available (might need multiple items per package)
        PackageDimension largest = null;
        for (PackageDimension pkg : packages) {
            if (pkg.getAvailableQuantity() > 0) {
                if (largest == null || pkg.getVolume() > largest.getVolume()) {
                    largest = pkg;
                }
            }
        }

        return largest;
    }
    
    // ============================================================================
    // Multi-Product Packaging Optimization
    // ============================================================================
    
    /**
     * Represents a single product item to be packed (one unit of a product)
     */
    public static class ProductItem {
        private final Long productId;
        private final double volume;
        private final double weight;
        
        public ProductItem(Long productId, double volume, double weight) {
            this.productId = productId;
            this.volume = volume;
            this.weight = weight;
        }
        
        public Long getProductId() { return productId; }
        public double getVolume() { return volume; }
        public double getWeight() { return weight; }
    }
    
    /**
     * Tracks which products are in a used package
     */
    public static class MultiProductUsedPackage {
        final PackageDimension packageDim;
        double usedVolume;
        double usedWeight;
        Map<Long, Integer> productCounts = new HashMap<>(); // productId -> count
        
        MultiProductUsedPackage(PackageDimension packageDim) {
            this.packageDim = packageDim;
            this.usedVolume = 0;
            this.usedWeight = 0;
        }
        
        double getRemainingVolume() {
            return packageDim.getVolume() - usedVolume;
        }
        
        double getRemainingWeight() {
            return packageDim.getMaxWeight() - usedWeight;
        }
        
        boolean canFit(double productVolume, double productWeight) {
            return getRemainingVolume() >= productVolume && getRemainingWeight() >= productWeight;
        }
        
        void addProduct(Long productId, double productVolume, double productWeight) {
            this.usedVolume += productVolume;
            this.usedWeight += productWeight;
            this.productCounts.merge(productId, 1, Integer::sum);
        }
        
        public Map<Long, Integer> getProductCounts() { return productCounts; }
        public PackageDimension getPackageDim() { return packageDim; }
    }
    
    /**
     * Result of multi-product packaging - includes which products are in each package
     */
    public static class MultiProductPackageUsageResult {
        private final Long packageId;
        private final String packageName;
        private final String packageType;
        private final int quantityUsed;
        private final BigDecimal pricePerUnit;
        private final BigDecimal totalCost;
        private final Map<Long, Integer> productQuantities; // productId -> quantity in this package type
        
        public MultiProductPackageUsageResult(Long packageId, String packageName, String packageType,
                                              int quantityUsed, BigDecimal pricePerUnit,
                                              Map<Long, Integer> productQuantities) {
            this.packageId = packageId;
            this.packageName = packageName;
            this.packageType = packageType;
            this.quantityUsed = quantityUsed;
            this.pricePerUnit = pricePerUnit;
            this.totalCost = pricePerUnit.multiply(BigDecimal.valueOf(quantityUsed));
            this.productQuantities = new HashMap<>(productQuantities);
        }
        
        public Long getPackageId() { return packageId; }
        public String getPackageName() { return packageName; }
        public String getPackageType() { return packageType; }
        public int getQuantityUsed() { return quantityUsed; }
        public BigDecimal getPricePerUnit() { return pricePerUnit; }
        public BigDecimal getTotalCost() { return totalCost; }
        public Map<Long, Integer> getProductQuantities() { return productQuantities; }
    }
    
    /**
     * Complete multi-product packaging estimate result
     */
    public static class MultiProductPackagingResult {
        private final List<MultiProductPackageUsageResult> packagesUsed;
        private final BigDecimal totalPackagingCost;
        private final int totalPackagesUsed;
        private final Map<Long, Integer> packedItemsByProduct; // productId -> items packed
        private final boolean canPackAllItems;
        private final String errorMessage;
        
        public MultiProductPackagingResult(List<MultiProductPackageUsageResult> packagesUsed,
                                           Map<Long, Integer> requestedItemsByProduct,
                                           Map<Long, Integer> packedItemsByProduct) {
            this.packagesUsed = packagesUsed;
            this.totalPackagesUsed = packagesUsed.stream().mapToInt(MultiProductPackageUsageResult::getQuantityUsed).sum();
            this.totalPackagingCost = packagesUsed.stream()
                    .map(MultiProductPackageUsageResult::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            new HashMap<>(requestedItemsByProduct);
            this.packedItemsByProduct = new HashMap<>(packedItemsByProduct);
            
            int totalRequested = requestedItemsByProduct.values().stream().mapToInt(Integer::intValue).sum();
            int totalPacked = packedItemsByProduct.values().stream().mapToInt(Integer::intValue).sum();
            this.canPackAllItems = totalPacked >= totalRequested;
            this.errorMessage = canPackAllItems ? null : 
                "Not enough packages to pack all items. Can only pack " + totalPacked + " of " + totalRequested + " items.";
        }
        
        public List<MultiProductPackageUsageResult> getPackagesUsed() { return packagesUsed; }
        public BigDecimal getTotalPackagingCost() { return totalPackagingCost; }
        public int getTotalPackagesUsed() { return totalPackagesUsed; }
        public Map<Long, Integer> getPackedItemsByProduct() { return packedItemsByProduct; }
        public boolean isCanPackAllItems() { return canPackAllItems; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Calculate optimal packaging for multiple products together.
     * Uses First Fit Decreasing bin-packing algorithm optimized for cost.
     * Different products can share the same package if they fit.
     *
     * @param products Map of productId to ProductDimension (with quantities)
     * @param availablePackages List of available packages at the location
     * @return MultiProductPackagingResult with packages needed, costs, and product assignments
     */
    public MultiProductPackagingResult calculatePackagingForMultipleProducts(
            Map<Long, ProductDimension> products, 
            List<PackageDimension> availablePackages) {
        
        Map<Long, Integer> requestedItems = new HashMap<>();
        Map<Long, Integer> packedItems = new HashMap<>();
        
        // Flatten all products into individual items
        List<ProductItem> allItems = new ArrayList<>();
        for (Map.Entry<Long, ProductDimension> entry : products.entrySet()) {
            Long productId = entry.getKey();
            ProductDimension product = entry.getValue();
            requestedItems.put(productId, product.getQuantity());
            packedItems.put(productId, 0);
            
            double volume = product.getVolume();
            double weight = product.getWeight();
            
            for (int i = 0; i < product.getQuantity(); i++) {
                allItems.add(new ProductItem(productId, volume, weight));
            }
        }
        
        if (allItems.isEmpty()) {
            return new MultiProductPackagingResult(Collections.emptyList(), requestedItems, packedItems);
        }
        
        // Sort items by volume (largest first) - First Fit Decreasing
        allItems.sort((a, b) -> Double.compare(b.getVolume(), a.getVolume()));
        
        // Create mutable copies of packages sorted by cost-efficiency
        List<PackageDimension> packages = new ArrayList<>();
        for (PackageDimension pkg : availablePackages) {
            packages.add(new PackageDimension(
                    pkg.getPackageId(),
                    pkg.getPackageName(),
                    pkg.getPackageType(),
                    pkg.length,
                    pkg.breadth,
                    pkg.height,
                    pkg.maxWeight,
                    pkg.pricePerUnit,
                    pkg.getAvailableQuantity()
            ));
        }
        
        // Sort packages by cost per volume (cheapest first)
        packages.sort((a, b) -> {
            double costPerVolumeA = a.getVolume() > 0 ? a.getPricePerUnit().doubleValue() / a.getVolume() : Double.MAX_VALUE;
            double costPerVolumeB = b.getVolume() > 0 ? b.getPricePerUnit().doubleValue() / b.getVolume() : Double.MAX_VALUE;
            return Double.compare(costPerVolumeA, costPerVolumeB);
        });
        
        // Track used packages
        List<MultiProductUsedPackage> usedPackages = new ArrayList<>();
        
        // Pack each item using First Fit Decreasing
        for (ProductItem item : allItems) {
            boolean packed = false;
            
            // First, try to fit in an already-used package (prioritize by remaining volume - best fit)
            usedPackages.sort((a, b) -> {
                // Prefer packages where item fits exactly (least remaining space after)
                double remainingA = a.getRemainingVolume() - item.getVolume();
                double remainingB = b.getRemainingVolume() - item.getVolume();
                // If item doesn't fit, push to end
                if (remainingA < 0) remainingA = Double.MAX_VALUE;
                if (remainingB < 0) remainingB = Double.MAX_VALUE;
                return Double.compare(remainingA, remainingB);
            });
            
            for (MultiProductUsedPackage usedPkg : usedPackages) {
                if (usedPkg.canFit(item.getVolume(), item.getWeight())) {
                    usedPkg.addProduct(item.getProductId(), item.getVolume(), item.getWeight());
                    packedItems.merge(item.getProductId(), 1, Integer::sum);
                    packed = true;
                    break;
                }
            }
            
            // If not packed, get a new package
            if (!packed) {
                PackageDimension newPackage = getNextSuitablePackage(packages, item.getVolume(), item.getWeight());
                if (newPackage != null) {
                    newPackage.decrementQuantity();
                    MultiProductUsedPackage usedPkg = new MultiProductUsedPackage(newPackage);
                    usedPkg.addProduct(item.getProductId(), item.getVolume(), item.getWeight());
                    usedPackages.add(usedPkg);
                    packedItems.merge(item.getProductId(), 1, Integer::sum);
                    packed = true;
                }
            }
            
            // If still not packed, we ran out of suitable packages
            if (!packed) {
                break;
            }
        }
        
        // Aggregate results by package type
        Map<Long, Integer> packageUsageCount = new HashMap<>();
        Map<Long, Map<Long, Integer>> productsByPackage = new HashMap<>(); // packageId -> (productId -> count)
        
        for (MultiProductUsedPackage usedPkg : usedPackages) {
            Long pkgId = usedPkg.getPackageDim().getPackageId();
            packageUsageCount.merge(pkgId, 1, Integer::sum);
            
            // Aggregate product counts for this package type
            Map<Long, Integer> pkgProducts = productsByPackage.computeIfAbsent(pkgId, k -> new HashMap<>());
            for (Map.Entry<Long, Integer> productEntry : usedPkg.getProductCounts().entrySet()) {
                pkgProducts.merge(productEntry.getKey(), productEntry.getValue(), Integer::sum);
            }
        }
        
        // Build result
        List<MultiProductPackageUsageResult> results = new ArrayList<>();
        for (PackageDimension pkg : availablePackages) {
            int count = packageUsageCount.getOrDefault(pkg.getPackageId(), 0);
            if (count > 0) {
                results.add(new MultiProductPackageUsageResult(
                        pkg.getPackageId(),
                        pkg.getPackageName(),
                        pkg.getPackageType(),
                        count,
                        pkg.getPricePerUnit(),
                        productsByPackage.getOrDefault(pkg.getPackageId(), Collections.emptyMap())
                ));
            }
        }
        
        // Sort results by package name for consistent display
        results.sort(Comparator.comparing(MultiProductPackageUsageResult::getPackageName));
        
        return new MultiProductPackagingResult(results, requestedItems, packedItems);
    }
}
