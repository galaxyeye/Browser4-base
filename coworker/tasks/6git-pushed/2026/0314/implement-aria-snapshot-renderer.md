# Implement AriaSnapshotRenderer

Create a new AriaSnapshotRenderer to render aria snapshot from EnhancedDOMTreeNode.

The old AriaSnapshotForNanoDOMTreeRenderer might lose some aria information when converting from EnhancedDOMTreeNode to NanoDOMTreeNode.
The new AriaSnapshotRenderer will directly render from EnhancedDOMTreeNode to ensure that all aria information is preserved in the snapshot.

Keep the old AriaSnapshotForNanoDOMTreeRenderer for backward compatibility, but it should be marked as deprecated and eventually removed in the future.

