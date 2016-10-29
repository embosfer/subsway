package org.jdesktop.swingx.prompt;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicTextUI;

import org.jdesktop.swingx.plaf.TextUIWrapper;

/**
 * Overriding the original BuddySupport class to change the Position enum below,
 * replacing LEFT by West and RIGHT by East, to fix the issue
 * "java.lang.IllegalArgumentException: cannot add to layout: unknown constraint: LEFT"
 * This is a temp patch until https://java.net/jira/browse/SWINGX-1597 is resolved
 * 
 * @author Karl Schaefer
 *
 */
public class BuddySupport {
	public enum Position {
		// LEFT, RIGHT
		West, East // replacing the original LEFT, RIGHT
	};

	public static final String OUTER_MARGIN = "outerMargin";

	public static void addLeft(Component c, JTextField textField) {
		add(c, Position.West, textField);
	}

	public static void addRight(Component c, JTextField textField) {
		add(c, Position.East, textField);
	}

	public static void add(Component c, Position pos, JTextField textField) {
		TextUIWrapper.getDefaultWrapper().install(textField, true);

		List<Component> leftBuddies = buddies(Position.West, textField);
		List<Component> rightBuddies = buddies(Position.East, textField);

		// ensure buddies are added
		setLeft(textField, leftBuddies);
		setRight(textField, rightBuddies);

		// check if component is already here
		if (isBuddy(c, textField)) {
			throw new IllegalStateException("Component already added.");
		}

		if (Position.West == pos) {
			leftBuddies.add(c);
		} else {
			rightBuddies.add(0, c);
		}

		addToComponentHierarchy(c, pos, textField);
	}

	public static void addGap(int width, Position pos, JTextField textField) {
		add(createGap(width), pos, textField);
	}

	public static void setRight(JTextField textField, List<Component> rightBuddies) {
		set(rightBuddies, Position.East, textField);
	}

	public static void setLeft(JTextField textField, List<Component> leftBuddies) {
		set(leftBuddies, Position.West, textField);
	}

	public static void set(List<Component> buddies, Position pos, JTextField textField) {
		textField.putClientProperty(pos, buddies);
	}

	private static void addToComponentHierarchy(Component c, Position pos, JTextField textField) {
		textField.add(c, pos.toString());
	}

	public static List<Component> getLeft(JTextField textField) {
		return getBuddies(Position.West, textField);
	}

	public static List<Component> getRight(JTextField textField) {
		return getBuddies(Position.East, textField);
	}

	public static List<Component> getBuddies(Position pos, JTextField textField) {
		return Collections.unmodifiableList(buddies(pos, textField));
	}

	@SuppressWarnings("unchecked")
	private static List<Component> buddies(Position pos, JTextField textField) {
		List<Component> buddies = (List<Component>) textField.getClientProperty(pos);

		if (buddies != null) {
			return buddies;
		}
		return new ArrayList<Component>();
	}

	public static boolean isBuddy(Component c, JTextField textField) {
		return buddies(Position.West, textField).contains(c) || buddies(Position.East, textField).contains(c);
	}

	/**
	 * Because {@link BasicTextUI} removes all components when uninstalled and
	 * therefore all buddies are removed when the LnF changes.
	 * 
	 * @param c
	 * @param textField
	 */
	public static void remove(JComponent c, JTextField textField) {
		buddies(Position.West, textField).remove(c);
		buddies(Position.East, textField).remove(c);

		textField.remove(c);
	}

	public static void removeAll(JTextField textField) {
		List<Component> left = buddies(Position.West, textField);
		for (Component c : left) {
			textField.remove(c);
		}
		left.clear();
		List<Component> right = buddies(Position.East, textField);
		for (Component c : right) {
			textField.remove(c);
		}
		right.clear();

	}

	public static void setOuterMargin(JTextField buddyField, Insets margin) {
		buddyField.putClientProperty(OUTER_MARGIN, margin);
	}

	public static Insets getOuterMargin(JTextField buddyField) {
		return (Insets) buddyField.getClientProperty(OUTER_MARGIN);
	}

	public static void ensureBuddiesAreInComponentHierarchy(JTextField textField) {
		for (Component c : BuddySupport.getLeft(textField)) {
			addToComponentHierarchy(c, Position.West, textField);
		}
		for (Component c : BuddySupport.getRight(textField)) {
			addToComponentHierarchy(c, Position.East, textField);
		}
	}

	/**
	 * Create a gap to insert between to buddies.
	 * 
	 * @param width
	 * @return
	 */
	public static Component createGap(int width) {
		return Box.createHorizontalStrut(width);
	}
}
