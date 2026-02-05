#!/usr/bin/env python3
"""
Script to add permission tests to test files.
Maps test class names to appropriate permission strings.
"""

import os
import re

# Mapping of test file patterns to permission strings
# Format: (file_pattern, test_class_keyword, permission_name, method_name_pattern)
PERMISSION_MAPPINGS = {
    "GetMessageDetailsByIdTest": ("VIEW_MESSAGE", "getMessage"),
    "GetMessagesByUserIdTest": ("VIEW_MESSAGE", "getMessage"),
    "GetMessagesInBatchesTest": ("VIEW_MESSAGE", "getMessage"),
    "GetUnreadMessageCountTest": ("VIEW_MESSAGE", "getMessage"),
    "SetMessageReadTest": ("UPDATE_MESSAGE", "setMessage"),
    "ToggleMessageTest": ("DELETE_MESSAGE", "toggleMessage"),
    "UpdateMessageTest": ("UPDATE_MESSAGE", "updateMessage"),
    "BulkCreateLeadsTest": ("INSERT_LEAD", "bulkCreate"),
    "GetLeadDetailsTest": ("VIEW_LEAD", "getLead"),
    "GetLeadsInBatchesTest": ("VIEW_LEAD", "getLead"),
    "ToggleLeadTest": ("DELETE_LEAD", "toggleLead"),
    "UpdateLeadTest": ("UPDATE_LEAD", "updateLead"),
    "ConfirmEmailTest": ("LOGIN", "confirm"),
    "GetTokenTest": ("LOGIN", "getToken"),
    "ResetPasswordTest": ("LOGIN", "resetPassword"),
    "CreatePackageTest": ("INSERT_PACKAGE", "createPackage"),
    "GetPackageByIdTest": ("VIEW_PACKAGE", "getPackage"),
    "GetPackagesByPickupLocationIdTest": ("VIEW_PACKAGE", "getPackage"),
    "GetPackagesInBatchesTest": ("VIEW_PACKAGE", "getPackage"),
    "TogglePackageTest": ("DELETE_PACKAGE", "togglePackage"),
    "UpdatePackageTest": ("UPDATE_PACKAGE", "updatePackage"),
    "BulkCreatePackagesTest": ("INSERT_PACKAGE", "bulkCreate"),
    "CreatePickupLocationTest": ("INSERT_PICKUP_LOCATION", "createPickup"),
    "GetPickupLocationByIdTest": ("VIEW_PICKUP_LOCATION", "getPickup"),
    "GetPickupLocationsInBatchesTest": ("VIEW_PICKUP_LOCATION", "getPickup"),
    "TogglePickupLocationTest": ("DELETE_PICKUP_LOCATION", "togglePickup"),
    "UpdatePickupLocationTest": ("UPDATE_PICKUP_LOCATION", "updatePickup"),
    "BulkCreatePickupLocationsTest": ("INSERT_PICKUP_LOCATION", "bulkCreate"),
}

def extract_class_name(filepath):
    """Extract the test class name from filepath."""
    return os.path.basename(filepath).replace(".java", "")

def find_closing_brace(content):
    """Find the line number of the closing brace of the class."""
    lines = content.split('\n')
    brace_count = 0
    in_class = False
    
    for i, line in enumerate(lines):
        if 'class ' in line and '{' in line:
            in_class = True
            brace_count += line.count('{') - line.count('}')
        elif in_class:
            brace_count += line.count('{') - line.count('}')
            if brace_count == 0:
                return i
    return len(lines) - 1

def add_permission_test(filepath, permission):
    """Add permission test before the closing brace of a test file."""
    with open(filepath, 'r') as f:
        content = f.read()
    
    class_name = extract_class_name(filepath)
    
    # Check if permission test already exists
    if f'hasAuthority("{permission}")' in content:
        print(f"✓ {class_name}: Permission test already exists")
        return True
    
    # Find closing brace
    closing_line = find_closing_brace(content)
    lines = content.split('\n')
    
    # Create permission test
    test_method = f'''
    @Test
    @DisplayName("{class_name} - Verifies authorization hasAuthority with {permission} permission")
    void {class_name[0].lower() + ''.join(re.sub(r'([A-Z])', r'_\1', class_name[1:]).lower())}_VerifyAuthorizationPermission() {{
        // This test verifies the permission check is performed
        verify(authorization, times(1)).hasAuthority("{permission}");
    }}
'''
    
    # Insert before closing brace
    lines.insert(closing_line, test_method)
    new_content = '\n'.join(lines)
    
    with open(filepath, 'w') as f:
        f.write(new_content)
    
    print(f"✓ {class_name}: Added {permission} permission test")
    return True

if __name__ == "__main__":
    base_path = "/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests"
    
    for filename, (permission, _) in PERMISSION_MAPPINGS.items():
        # Find the file
        for root, dirs, files in os.walk(base_path):
            for file in files:
                if file == f"{filename}.java":
                    filepath = os.path.join(root, file)
                    add_permission_test(filepath, permission)
                    break
