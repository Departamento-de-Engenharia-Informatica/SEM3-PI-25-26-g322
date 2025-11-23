import isep.ipp.pt.g322.datastructures.tree.AVL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AVLTest {
    private AVL<Integer> avl;

    @BeforeEach
    void setUp() {
        avl = new AVL<>();
    }

    @Test
    void testInsert_SingleElement() {
        avl.insert(10);

        assertEquals(1, avl.size(), "Tree should have 1 element");
        assertFalse(avl.isEmpty(), "Tree should not be empty");
        assertEquals(Integer.valueOf(10), avl.find(10), "Should find inserted element");
    }

    @Test
    void testInsert_MultipleElements() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        assertEquals(3, avl.size(), "Tree should have 3 elements");
        assertEquals(Integer.valueOf(10), avl.find(10));
        assertEquals(Integer.valueOf(5), avl.find(5));
        assertEquals(Integer.valueOf(15), avl.find(15));
    }

    @Test
    void testInsert_DuplicateElement_UpdatesValue() {
        avl.insert(10);
        avl.insert(10);

        assertEquals(1, avl.size(), "Duplicate insert should not increase size");
        assertEquals(Integer.valueOf(10), avl.find(10));
    }

    @Test
    void testInsert_AscendingOrder_TriggersLeftRotation() {
        avl.insert(10);
        avl.insert(20);
        avl.insert(30);

        assertEquals(3, avl.size());
        assertEquals(1, avl.height(), "Height should be 2 after balancing");
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(20));
        assertNotNull(avl.find(30));
    }

    @Test
    void testInsert_DescendingOrder_TriggersRightRotation() {
        avl.insert(30);
        avl.insert(20);
        avl.insert(10);

        assertEquals(3, avl.size());
        assertEquals(1, avl.height(), "Height should be 2 after balancing");
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(20));
        assertNotNull(avl.find(30));
    }

    @Test
    void testInsert_LeftRightCase_TriggersTwoRotations() {
        avl.insert(30);
        avl.insert(10);
        avl.insert(20);

        assertEquals(3, avl.size());
        assertEquals(1, avl.height(), "Height should be 2 after balancing");
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(20));
        assertNotNull(avl.find(30));
    }

    @Test
    void testInsert_RightLeftCase_TriggersTwoRotations() {
        avl.insert(10);
        avl.insert(30);
        avl.insert(20);

        assertEquals(3, avl.size());
        assertEquals(1, avl.height(), "Height should be 2 after balancing");
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(20));
        assertNotNull(avl.find(30));
    }

    @Test
    void testInsert_ManyElements_MaintainsBalance() {
        for (int i = 1; i <= 15; i++) {
            avl.insert(i);
        }

        assertEquals(15, avl.size());
        assertTrue(avl.height() <= 5, "Height should be logarithmic: " + avl.height());

        for (int i = 1; i <= 15; i++) {
            assertNotNull(avl.find(i), "Should find element " + i);
        }
    }

    @Test
    void testInsert_RandomOrder() {
        int[] values = {50, 25, 75, 10, 30, 60, 80, 5, 15, 27, 55, 65};

        for (int value : values) {
            avl.insert(value);
        }

        assertEquals(values.length, avl.size());
        assertTrue(avl.height() <= 5, "Height should remain balanced");

        for (int value : values) {
            assertNotNull(avl.find(value));
        }
    }


    @Test
    void testFind_ExistingElement() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        assertEquals(Integer.valueOf(10), avl.find(10));
        assertEquals(Integer.valueOf(5), avl.find(5));
        assertEquals(Integer.valueOf(15), avl.find(15));
    }

    @Test
    void testFind_NonExistingElement() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        assertNull(avl.find(20), "Should return null for non-existent element");
        assertNull(avl.find(1), "Should return null for non-existent element");
        assertNull(avl.find(12), "Should return null for non-existent element");
    }

    @Test
    void testFind_EmptyTree() {
        assertNull(avl.find(10), "Should return null in empty tree");
    }

    @Test
    void testFind_AfterMultipleInsertions() {
        for (int i = 0; i < 100; i++) {
            avl.insert(i);
        }

        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i), avl.find(i), "Should find element " + i);
        }

        assertNull(avl.find(100), "Should not find element 100");
        assertNull(avl.find(-1), "Should not find element -1");
    }

    @Test
    void testFind_SingleElement() {
        avl.insert(42);

        assertEquals(Integer.valueOf(42), avl.find(42));
        assertNull(avl.find(41));
        assertNull(avl.find(43));
    }


    @Test
    void testFindRange_AllElementsInRange() {
        avl.insert(5);
        avl.insert(3);
        avl.insert(7);
        avl.insert(2);
        avl.insert(4);
        avl.insert(6);
        avl.insert(8);

        List<Integer> result = avl.findRange(2, 8);

        assertEquals(7, result.size(), "Should find all 7 elements");
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
        assertTrue(result.contains(4));
        assertTrue(result.contains(5));
        assertTrue(result.contains(6));
        assertTrue(result.contains(7));
        assertTrue(result.contains(8));
    }

    @Test
    void testFindRange_PartialRange() {
        for (int i = 1; i <= 10; i++) {
            avl.insert(i);
        }

        List<Integer> result = avl.findRange(3, 7);

        assertEquals(5, result.size(), "Should find elements 3-7");
        assertTrue(result.contains(3));
        assertTrue(result.contains(4));
        assertTrue(result.contains(5));
        assertTrue(result.contains(6));
        assertTrue(result.contains(7));
        assertFalse(result.contains(2));
        assertFalse(result.contains(8));
    }

    @Test
    void testFindRange_SingleElement() {
        for (int i = 1; i <= 10; i++) {
            avl.insert(i);
        }

        List<Integer> result = avl.findRange(5, 5);

        assertEquals(1, result.size(), "Should find only element 5");
        assertEquals(Integer.valueOf(5), result.get(0));
    }

    @Test
    void testFindRange_NoElementsInRange() {
        avl.insert(10);
        avl.insert(20);
        avl.insert(30);

        List<Integer> result = avl.findRange(12, 18);

        assertTrue(result.isEmpty(), "Should return empty list when no elements in range");
    }

    @Test
    void testFindRange_EmptyTree() {
        List<Integer> result = avl.findRange(1, 10);

        assertNotNull(result, "Should return non-null list");
        assertTrue(result.isEmpty(), "Should return empty list for empty tree");
    }

    @Test
    void testFindRange_MinGreaterThanMax() {
        avl.insert(5);
        avl.insert(3);
        avl.insert(7);

        List<Integer> result = avl.findRange(7, 3);

        assertTrue(result.isEmpty(), "Should return empty when min > max");
    }

    @Test
    void testFindRange_BoundaryInclusive() {
        avl.insert(5);
        avl.insert(3);
        avl.insert(7);
        avl.insert(4);
        avl.insert(6);

        List<Integer> result = avl.findRange(3, 7);

        assertTrue(result.contains(3), "Should include lower boundary");
        assertTrue(result.contains(7), "Should include upper boundary");
        assertEquals(5, result.size());
    }

    @Test
    void testFindRange_OutOfTreeBounds() {
        avl.insert(10);
        avl.insert(20);
        avl.insert(30);

        List<Integer> result = avl.findRange(5, 35);

        assertEquals(3, result.size(), "Should find all elements even when range exceeds tree bounds");
    }

    @Test
    void testFindRange_LargeRange() {
        for (int i = 0; i < 100; i += 10) {
            avl.insert(i);
        }

        List<Integer> result = avl.findRange(20, 70);

        assertEquals(6, result.size(), "Should find elements 20, 30, 40, 50, 60, 70");
    }


    @Test
    void testRemove_LeafNode() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        avl.remove(5);

        assertEquals(2, avl.size(), "Size should decrease after removal");
        assertNull(avl.find(5), "Removed element should not be found");
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(15));
    }

    @Test
    void testRemove_NodeWithOneChild_LeftChild() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(3);

        avl.remove(5);

        assertEquals(2, avl.size());
        assertNull(avl.find(5));
        assertNotNull(avl.find(3));
        assertNotNull(avl.find(10));
    }

    @Test
    void testRemove_NodeWithOneChild_RightChild() {
        avl.insert(10);
        avl.insert(15);
        avl.insert(20);

        avl.remove(15);

        assertEquals(2, avl.size());
        assertNull(avl.find(15));
        assertNotNull(avl.find(10));
        assertNotNull(avl.find(20));
    }

    @Test
    void testRemove_NodeWithTwoChildren() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);
        avl.insert(3);
        avl.insert(7);
        avl.insert(12);
        avl.insert(20);

        avl.remove(10);

        assertEquals(6, avl.size());
        assertNull(avl.find(10), "Removed element should not be found");
        assertNotNull(avl.find(5));
        assertNotNull(avl.find(15));
        assertNotNull(avl.find(3));
        assertNotNull(avl.find(7));
        assertNotNull(avl.find(12));
        assertNotNull(avl.find(20));
    }

    @Test
    void testRemove_RootNode() {
        avl.insert(10);

        avl.remove(10);

        assertEquals(0, avl.size());
        assertTrue(avl.isEmpty());
        assertNull(avl.find(10));
    }

    @Test
    void testRemove_NonExistingElement() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        int sizeBefore = avl.size();
        avl.remove(20);

        assertEquals(sizeBefore, avl.size(), "Size should not change when removing non-existent element");
    }

    @Test
    void testRemove_FromEmptyTree() {
        avl.remove(10);

        assertEquals(0, avl.size());
        assertTrue(avl.isEmpty());
    }

    @Test
    void testRemove_MaintainsBalance() {
        for (int i = 1; i <= 15; i++) {
            avl.insert(i);
        }

        int heightBefore = avl.height();

        avl.remove(8);
        avl.remove(4);
        avl.remove(12);

        assertEquals(12, avl.size());
        assertTrue(avl.height() <= heightBefore + 1, "Tree should remain balanced after removals");
    }

    @Test
    void testRemove_MultipleElementsSequentially() {
        for (int i = 1; i <= 10; i++) {
            avl.insert(i);
        }

        for (int i = 1; i <= 5; i++) {
            avl.remove(i);
            assertEquals(10 - i, avl.size(), "Size should decrease with each removal");
            assertNull(avl.find(i), "Removed element should not be found");
        }

        for (int i = 6; i <= 10; i++) {
            assertNotNull(avl.find(i), "Non-removed element " + i + " should still exist");
        }
    }

    @Test
    void testRemove_AllElements() {
        for (int i = 1; i <= 10; i++) {
            avl.insert(i);
        }

        for (int i = 1; i <= 10; i++) {
            avl.remove(i);
        }

        assertEquals(0, avl.size());
        assertTrue(avl.isEmpty());
        assertEquals(-1, avl.height());
    }

    @Test
    void testRemove_CausesLeftRotation() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(20);
        avl.insert(15);
        avl.insert(30);
        avl.insert(25);

        avl.remove(5);

        assertEquals(5, avl.size());
        assertNull(avl.find(5));
        assertTrue(avl.height() <= 3, "Tree should be balanced after removal");
    }

    @Test
    void testRemove_CausesRightRotation() {
        avl.insert(20);
        avl.insert(10);
        avl.insert(30);
        avl.insert(5);
        avl.insert(15);
        avl.insert(3);

        avl.remove(30);

        assertEquals(5, avl.size());
        assertNull(avl.find(30));
        assertTrue(avl.height() <= 3, "Tree should be balanced after removal");
    }

    @Test
    void testSize_EmptyTree() {
        assertEquals(0, avl.size());
    }

    @Test
    void testSize_AfterInsertions() {
        for (int i = 0; i < 50; i++) {
            avl.insert(i);
            assertEquals(i + 1, avl.size(), "Size should increase with each insertion");
        }
    }

    @Test
    void testSize_AfterInsertionsAndRemovals() {
        for (int i = 0; i < 20; i++) {
            avl.insert(i);
        }

        for (int i = 0; i < 10; i++) {
            avl.remove(i);
        }

        assertEquals(10, avl.size());
    }

    @Test
    void testHeight_EmptyTree() {
        assertEquals(-1, avl.height(), "Empty tree should have height -1");
    }

    @Test
    void testHeight_SingleNode() {
        avl.insert(10);
        assertEquals(0, avl.height());
    }

    @Test
    void testHeight_BalancedTree() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        assertEquals(1, avl.height());
    }

    @Test
    void testEquals_SameTree() {
        assertTrue(avl.equals(avl), "Tree should equal itself");
    }

    @Test
    void testEquals_TwoEmptyTrees() {
        AVL<Integer> other = new AVL<>();

        assertTrue(avl.equals(other), "Two empty trees should be equal");
    }

    @Test
    void testEquals_SameElements() {
        AVL<Integer> avl1 = new AVL<>();
        AVL<Integer> avl2 = new AVL<>();

        int[] values = {10, 5, 15, 3, 7, 12, 20};
        for (int value : values) {
            avl1.insert(value);
            avl2.insert(value);
        }

        assertTrue(avl1.equals(avl2), "Trees with same elements should be equal");
    }

    @Test
    void testEquals_DifferentElements() {
        AVL<Integer> avl1 = new AVL<>();
        AVL<Integer> avl2 = new AVL<>();

        avl1.insert(10);
        avl1.insert(5);
        avl1.insert(15);

        avl2.insert(10);
        avl2.insert(5);
        avl2.insert(16);

        assertFalse(avl1.equals(avl2), "Trees with different elements should not be equal");
    }

    @Test
    void testEquals_DifferentSizes() {
        AVL<Integer> avl1 = new AVL<>();
        AVL<Integer> avl2 = new AVL<>();

        avl1.insert(10);
        avl1.insert(5);
        avl1.insert(15);

        avl2.insert(10);
        avl2.insert(5);

        assertFalse(avl1.equals(avl2), "Trees with different sizes should not be equal");
    }

    @Test
    void testEquals_NullComparison() {
        assertFalse(avl.equals(null), "Tree should not equal null");
    }

    @Test
    void testEquals_DifferentType() {
        assertFalse(avl.equals("not an AVL tree"), "Tree should not equal different type");
    }

    @Test
    void testEquals_SameElementsDifferentInsertionOrder() {
        AVL<Integer> avl1 = new AVL<>();
        AVL<Integer> avl2 = new AVL<>();

        avl1.insert(10);
        avl1.insert(5);
        avl1.insert(15);
        avl1.insert(3);

        avl2.insert(5);
        avl2.insert(15);
        avl2.insert(10);
        avl2.insert(3);

        assertTrue(avl1.equals(avl2), "AVL trees with same elements should be equal regardless of insertion order");
    }

    @Test
    void testEquals_AfterRemoval() {
        AVL<Integer> avl1 = new AVL<>();
        AVL<Integer> avl2 = new AVL<>();

        for (int i = 1; i <= 10; i++) {
            avl1.insert(i);
            avl2.insert(i);
        }

        avl1.remove(5);
        avl2.remove(5);

        assertTrue(avl1.equals(avl2), "Trees should be equal after same removal");
    }

    @Test
    void testIsEmpty_NewTree() {
        assertTrue(avl.isEmpty(), "New tree should be empty");
    }

    @Test
    void testIsEmpty_AfterInsertion() {
        avl.insert(10);
        assertFalse(avl.isEmpty(), "Tree should not be empty after insertion");
    }

    @Test
    void testIsEmpty_AfterRemovingAllElements() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);

        avl.remove(10);
        avl.remove(5);
        avl.remove(15);

        assertTrue(avl.isEmpty(), "Tree should be empty after removing all elements");
    }

    @Test
    void testSmallestElement_SingleNode() {
        avl.insert(10);
        assertEquals(Integer.valueOf(10), avl.smallestElement());
    }

    @Test
    void testSmallestElement_MultipleNodes() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);
        avl.insert(3);
        avl.insert(7);

        assertEquals(Integer.valueOf(3), avl.smallestElement());
    }

    @Test
    void testSmallestElement_AfterRemoval() {
        avl.insert(10);
        avl.insert(5);
        avl.insert(15);
        avl.insert(3);

        avl.remove(3);

        assertEquals(Integer.valueOf(5), avl.smallestElement());
    }
}