SRC_MAIN := src/main/java
SRC_TEST := src/test/java
RES_TEST := src/test/resources

OUT_DIR := out
OUT_MAIN := $(OUT_DIR)/main
OUT_TEST := $(OUT_DIR)/test

LIB_DIR := lib
JUNIT := $(LIB_DIR)/junit-platform-console-standalone-6.0.1.jar

CLASSPATH_MAIN := $(OUT_MAIN):$(JUNIT)


.PHONY: all clean test compile compile-main compile-test help deps

help:
	@echo ""
	@echo "Available targets:"
	@echo "  help            Show this help message"
	@echo "  deps            Download dependencies (JUnit)"
	@echo "  compile         Compile main and test sources"
	@echo "  compile-main    Compile only main sources"
	@echo "  compile-test    Compile only test sources (depends on compile-main)"
	@echo "  test            Run JUnit tests"
	@echo "  clean           Remove build output"
	@echo ""


all: compile


compile: deps compile-main compile-test


compile-main:
	@echo "== Compiling main sources =="
	mkdir -p $(OUT_MAIN)
	javac -cp $(JUNIT) -d $(OUT_MAIN) $$(find $(SRC_MAIN) -name "*.java")


compile-test: compile-main
	@echo "== Compiling test sources =="
	mkdir -p $(OUT_TEST)
	javac -cp $(CLASSPATH_MAIN) -d $(OUT_TEST) $$(find $(SRC_TEST) -name "*.java")

	@echo "== Copying test resources =="
	@if [ -d $(RES_TEST) ]; then cp -r $(RES_TEST)/* $(OUT_TEST)/ 2>/dev/null || true; fi


test: compile
	@echo "== Running JUnit tests =="
	java -jar $(JUNIT) \
		execute \
	     --classpath "$(OUT_MAIN):$(OUT_TEST)" \
	     --scan-classpath

clean:
	rm -rf $(OUT_DIR)


deps: $(JUNIT)

$(JUNIT): | $(LIB_DIR)
	wget -q https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/6.0.1/junit-platform-console-standalone-6.0.1.jar \
	     -O $(JUNIT)
	@echo "Downloaded JUnit Platform Console 6.0.1"

$(LIB_DIR):
	mkdir -p $(LIB_DIR)
