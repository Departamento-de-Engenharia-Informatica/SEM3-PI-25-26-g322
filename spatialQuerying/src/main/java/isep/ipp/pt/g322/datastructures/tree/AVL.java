package isep.ipp.pt.g322.datastructures.tree;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DEI-ESINF
 * @param <E>
 */
public class AVL <E extends Comparable<E>> extends BST<E> {


    private int balanceFactor(Node<E> node){
        return height(node.getRight()) - height(node.getLeft());
    }

    private Node<E> rightRotation(Node<E> node){
        Node<E> leftson = node.getLeft();
        node.setLeft(leftson.getRight());
        leftson.setRight(node);
        node = leftson;
        return node;    }

    private Node<E> leftRotation(Node<E> node){

        Node<E> rightson = node.getRight();
        node.setRight(rightson.getLeft());
        rightson.setLeft(node);
        node = rightson;
        return node;    }

    private Node<E> twoRotations(Node<E> node){

        if (balanceFactor(node) < 0) {
            node.setLeft(leftRotation(node.getLeft()));
            node = rightRotation(node);
        } else {
            node.setRight(rightRotation(node.getRight()));
            node = leftRotation(node);
        }
        return node;    }

    private Node<E> balanceNode(Node<E> node)
    {

        if (balanceFactor(node) < -1){
            int leftNodeBF = balanceFactor(node.getLeft());

            if (leftNodeBF <= 0){
                node = rightRotation(node);
            }else{
                node = twoRotations(node);
            }

        }

        if (balanceFactor(node) > 1){
            int rightNodeBF = balanceFactor(node.getRight());
            if (rightNodeBF >= 0){
                node = leftRotation(node);
            }else{
                node = twoRotations(node);
            }

        }
        return node;
    }

    public E find(E element) {
        Node<E> node = find(root, element);
        return (node != null) ? node.getElement() : null;
    }

    protected Node<E> find(Node<E> node, E element){
        if(node == null) {
            return null;
        }
        int cmp = element.compareTo(node.getElement());
        if(cmp == 0) {
            return node;
        }
        if(cmp < 0) {
            return find(node.getLeft(), element);
        }
        return find(node.getRight(), element);
    }


    @Override
    public void insert(E element){
        root = insert(element, root);
    }
    private Node<E> insert(E element, Node<E> node){
        if (node == null) return new Node(element, null, null);
        if (node.getElement().compareTo(element) == 0){

            node.setElement(element);

        }else{
            if (element.compareTo(node.getElement()) > 0){
                node.setRight(insert(element, node.getRight()));
                node = balanceNode(node);
            }
            if (element.compareTo(node.getElement()) < 0){
                node.setLeft(insert(element, node.getLeft()));
                node = balanceNode(node);
            }
        }
        return node;
    }

    public List<E> findRange(E min, E max) {
        List<E> result = new ArrayList<>();
        findRange(root, min, max, result);
        return result;
    }

    private void findRange(Node<E> node, E min, E max, List<E> result) {
        if (node == null) return;

        int cmpMin = node.getElement().compareTo(min);
        int cmpMax = node.getElement().compareTo(max);

        if (cmpMin > 0) findRange(node.getLeft(), min, max, result);
        if (cmpMin >= 0 && cmpMax <= 0) result.add(node.getElement());
        if (cmpMax < 0) findRange(node.getRight(), min, max, result);
    }

    @Override
    public void remove(E element){
        root = remove(element, root());
    }

    private Node<E> remove(E element, BST.Node<E> node) {
        if (node == null)
            return null;
        if (element.compareTo(node.getElement()) == 0) {
            if (node.getLeft() == null && node.getRight() == null)
                return null;
            if (node.getLeft() == null)
                return node.getRight();
            if (node.getRight() == null)
                return node.getLeft();

            E smallElem = smallestElement(node.getRight());
            node.setElement(smallElem);
            node.setRight(remove(smallElem, node.getRight()));
            node = balanceNode(node);
        }
        else if (element.compareTo(node.getElement()) < 0) {
            node.setLeft(remove(element,node.getLeft()));
            node = balanceNode(node);
        }
        else {
            node.setRight(remove(element,node.getRight()));
            node = balanceNode(node);
        }
        return node;    }


    public boolean equals(Object otherObj) {

        if (this == otherObj)
            return true;

        if (otherObj == null || this.getClass() != otherObj.getClass())
            return false;

        AVL<E> second = (AVL<E>) otherObj;
        return equals(root, second.root);
    }

    public boolean equals(Node<E> root1, Node<E> root2) {
        if (root1 == null && root2 == null)
            return true;
        else if (root1 != null && root2 != null) {
            if (root1.getElement().compareTo(root2.getElement()) == 0) {
                return equals(root1.getLeft(), root2.getLeft())
                        && equals(root1.getRight(), root2.getRight());
            } else
                return false;
        }
        else return false;
    }

}