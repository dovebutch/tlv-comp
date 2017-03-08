
add_subdirectory(joptsimple)
add_subdirectory(tlv)



add_jar(svgen AppLauncher.java) 

add_dependencies(svgen tlv)

add_custom_command(TARGET svgen 
	POST_BUILD
	COMMAND jar ufe svgen.jar AppLauncher
	COMMAND jar uf svgen.jar -C ${SVGEN_BUILD_DIR}/joptsimple joptsimple.jar 
	COMMAND jar uf svgen.jar -C ${SVGEN_BUILD_DIR}/tlv tlv.jar
	)


install_jar(svgen ${SVGEN_TARGET_DIR})

#set(SVGEN_SRC
#	tlv/Main.java
#	tlv/behavioral/Alignment.java
#	tlv/behavioral/LogicalBehHier.java
#	tlv/behavioral/LogicalBehScope.java
#	tlv/behavioral/LogicalBranch.java
#	tlv/behavioral/LogicalNode.java
#	tlv/behavioral/LogicalPipeline.java
#	tlv/behavioral/LogicalStage.java
#	tlv/behavioral/LogicalWhen.java
#	tlv/behavioral/PipeSignal.java
#	tlv/behavioral/TransitionStage.java
#	tlv/behavioral/range/AssignedBitRange.java
#	tlv/behavioral/range/BitRange.java
#	tlv/behavioral/range/Range.java
#	tlv/behavioral/range/TransitionRange.java
#	tlv/behavioral/range/UsedBitRange.java
#	tlv/config/IdentifierType.java
#	tlv/config/ProjSpecific.java
#	tlv/parse/Assignment.java
#	tlv/parse/NodeType.java
#	tlv/parse/ParseBehHier.java
#	tlv/parse/ParseBehScope.java
#	tlv/parse/ParseBranch.java
#	tlv/parse/ParseContext.java
#	tlv/parse/ParseNode.java
#	tlv/parse/ParsePipeline.java
#	tlv/parse/ParseSV.java
#	tlv/parse/ParseTLV.java
#	tlv/parse/ParseTLVVersion.java
#	tlv/parse/ParseStage.java
#	tlv/parse/ParseWhen.java
#	tlv/parse/Prefix.java
#	tlv/parse/SourceFile.java
#	tlv/parse/identifier/Expression.java
#	tlv/parse/identifier/Identifier.java
#	tlv/parse/identifier/ParseElement.java
#	tlv/parse/identifier/ParseMnemonic.java
#	tlv/utilities/Severity.java
#)
#
#set(JOPTSIMPLE_SRC 
#	joptsimple/AbstractOptionSpec.java
#	joptsimple/AlternativeLongOptionSpec.java
#	joptsimple/ArgumentAcceptingOptionSpec.java
#	joptsimple/ArgumentList.java
#	joptsimple/BuiltinHelpFormatter.java
#	joptsimple/HelpFormatter.java
#	joptsimple/IllegalOptionSpecificationException.java
#	joptsimple/MissingRequiredOptionException.java
#	joptsimple/MultipleArgumentsForOptionException.java
#	joptsimple/NoArgumentOptionSpec.java
#	joptsimple/OptionArgumentConversionException.java
#	joptsimple/OptionDescriptor.java
#	joptsimple/OptionException.java
#	joptsimple/OptionMissingRequiredArgumentException.java
#	joptsimple/OptionParser.java
#	joptsimple/OptionParserState.java
#	joptsimple/OptionSet.java
#	joptsimple/OptionSpec.java
#	joptsimple/OptionSpecBuilder.java
#	joptsimple/OptionSpecTokenizer.java
#	joptsimple/OptionalArgumentOptionSpec.java
#	joptsimple/ParserRules.java
#	joptsimple/RequiredArgumentOptionSpec.java
#	joptsimple/UnrecognizedOptionException.java
#	joptsimple/ValueConversionException.java
#	joptsimple/ValueConverter.java
#	joptsimple/internal/AbbreviationMap.java
#	joptsimple/internal/Classes.java
#	joptsimple/internal/Column.java
#	joptsimple/internal/ColumnWidthCalculator.java
#	joptsimple/internal/ColumnarData.java
#	joptsimple/internal/ConstructorInvokingValueConverter.java
#	joptsimple/internal/MethodInvokingValueConverter.java
#	joptsimple/internal/Objects.java
#	joptsimple/internal/Reflection.java
#	joptsimple/internal/ReflectionException.java
#	joptsimple/internal/Strings.java
#	joptsimple/util/DateConverter.java
#	joptsimple/util/KeyValuePair.java
#	joptsimple/util/RegexMatcher.java)
#
##set(CMAKE_JAVA_JAR_ENTRY_POINT tlv/Main) # this only works for cmake 2.8.8
#set(CMAKE_JAVA_TARGET_OUTPUT_DIR {$SVGEN_TARGET_DIR})
#add_jar(svgen ${JOPTSIMPLE_SRX} ${SVGEN_SRC})
#
#add_custom_command(TARGET svgen POST_BUILD COMMAND jar ufe svgen.jar tlv.Main)
#
#install_jar(svgen ${SVGEN_TARGET_DIR})
