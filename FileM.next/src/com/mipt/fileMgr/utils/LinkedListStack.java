package com.mipt.fileMgr.utils;

import java.util.EmptyStackException;

/**
 * 
 * @author fang
 * 
 */
public class LinkedListStack {

	private static class Node {
		Object o;
		Node next;
	}

	private Node top = null;

	public boolean imEmpty() {
		return top == null;
	}

	public Object peek() {
		if (top == null)
			throw new EmptyStackException();
		return top.o;
	}

	public void push(Object o) {
		Node temp = new Node();
		temp.o = o;
		temp.next = top;
		top = temp;
	}

	public Object pop() {
		if (top == null)
			return null;

		Object o = top.o;
		top = top.next;
		return o;
	}
}