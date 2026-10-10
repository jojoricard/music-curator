package com.musiccurator.gametest;

// Client game tests come without an assertion library; a failed check throws, which fails the test run.
final class Check {
	private Check() {
	}

	static void that(boolean condition, String message, Object... args) {
		if (!condition) {
			throw new AssertionError(String.format(message, args));
		}
	}
}
