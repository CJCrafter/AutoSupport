package me.cjcrafter.autosupport;

import java.util.function.Predicate;

/**
 * This interface is used by EACH registered {@link SupportData} to determine
 * if a given question (Which has already passed the
 * {@link Activator#test(Object)} test) is asking for THAT question.
 *
 * <p>For example, if your {@link SupportData} says, <code>"You can find all
 * dependencies on our GitHub"</code>, then this should return <code>true</code>
 * if the user asks, <code>"Where can I find the dependencies?"</code>
 */
@FunctionalInterface
public interface StringMatcher extends Predicate<String> {
}
