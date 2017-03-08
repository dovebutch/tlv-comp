

set(JOPTSIMPLE_SRC 
	AbstractOptionSpec.java
	AlternativeLongOptionSpec.java
	ArgumentAcceptingOptionSpec.java
	ArgumentList.java
	BuiltinHelpFormatter.java
	HelpFormatter.java
	IllegalOptionSpecificationException.java
	MissingRequiredOptionException.java
	MultipleArgumentsForOptionException.java
	NoArgumentOptionSpec.java
	OptionArgumentConversionException.java
	OptionDescriptor.java
	OptionException.java
	OptionMissingRequiredArgumentException.java
	OptionParser.java
	OptionParserState.java
	OptionSet.java
	OptionSpec.java
	OptionSpecBuilder.java
	OptionSpecTokenizer.java
	OptionalArgumentOptionSpec.java
	ParserRules.java
	RequiredArgumentOptionSpec.java
	UnrecognizedOptionException.java
	ValueConversionException.java
	ValueConverter.java
	internal/AbbreviationMap.java
	internal/Classes.java
	internal/Column.java
	internal/ColumnWidthCalculator.java
	internal/ColumnarData.java
	internal/ConstructorInvokingValueConverter.java
	internal/MethodInvokingValueConverter.java
	internal/Objects.java
	internal/Reflection.java
	internal/ReflectionException.java
	internal/Strings.java
	util/DateConverter.java
	util/KeyValuePair.java
	util/RegexMatcher.java)

add_jar(joptsimple ${JOPTSIMPLE_SRC})

#install_jar(joptsimple ${SVGEN_TARGET_DIR})
