

set(JOPTSIMPLE_SRC 
	AbstractOptionSpec.java
	AlternativeLongOptionSpec.java
	ArgumentAcceptingOptionSpec.java
	ArgumentList.java
	BuiltinHelpFormatter.java
	HelpFormatter.java
	IllegalOptionSpecificationException.java
	MissingRequiredOptionsException.java
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
	internal/Columns.java
	internal/ConstructorInvokingValueConverter.java
	internal/MethodInvokingValueConverter.java
	internal/Reflection.java
	internal/ReflectionException.java
	internal/Strings.java
	internal/Messages.java
	internal/Row.java
	internal/Rows.java
	util/DateConverter.java
	util/KeyValuePair.java
	util/RegexMatcher.java
	NonOptionArgumentSpec.java
	OptionDeclarer.java
	UnavailableOptionException.java
	UnconfiguredOptionException.java
	internal/Columns.java
	internal/Messages.java
	internal/OptionNameMap.java
	internal/Row.java
	internal/Rows.java
	internal/SimpleOptionNameMap.java
	util/EnumConverter.java
	util/InetAddressConverter.java
	util/PathConverter.java
	util/PathProperties.java
)

add_jar(joptsimple ${JOPTSIMPLE_SRC})

#install_jar(joptsimple ${SVGEN_TARGET_DIR})
