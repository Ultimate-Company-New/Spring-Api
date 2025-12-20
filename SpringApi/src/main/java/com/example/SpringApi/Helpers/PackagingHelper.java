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
        int itemCount;

        UsedPackage(PackageDimension packageDim) {
            this.packageDim = packageDim;
            this.usedVolume = 0;
            this.usedWeight = 0;
            this.itemCount = 0;
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
            this.itemCount++;
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
}
