package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.ebnf.extension.EBNFExtension
import il.ac.openu.flue.model.rule.Variable

import static il.ac.openu.flue.JavaEbnf.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**
 * @author Noam Rotem
 */
class JavaEbnf {
    static enum V implements Variable {Identifier, IdentifierChars, JavaLetter, JavaLetterOrDigit, TypeIdentifier,
    UnqualifiedMethodIdentifier, Literal, IntegerLiteral, FloatingPointLiteral, BooleanLiteral, CharacterLiteral,
    StringLiteral, NullLiteral, Type, PrimitiveType, ReferenceType, Annotation, NumericType, IntegralType,
    FloatingPointType, ClassOrInterfaceType, TypeVariable, ArrayType, ClassType, InterfaceType, TypeArguments,
    PackageName, Dims, TypeParameter, TypeParameterModifier, TypeBound, AdditionalBound, TypeArgumentList, TypeArgument,
    Wildcard, WildcardBounds, ModuleName, TypeName, PackageOrTypeName, ExpressionName, AmbiguousName, MethodName,
    CompilationUnit, OrdinaryCompilationUnit, PackageDeclaration, TypeDeclaration, ModularCompilationUnit,
    ImportDeclaration, ModuleDeclaration, PackageModifier, SingleTypeImportDeclaration, TypeImportOnDemandDeclaration,
    SingleStaticImportDeclaration, StaticImportOnDemandDeclaration, ClassDeclaration, InterfaceDeclaration,
    ModuleDirective, RequiresModifier, NormalClassDeclaration, EnumDeclaration, ClassModifier, TypeParameters,
    Superclass, Superinterfaces, ClassBody, TypeParameterList, InterfaceTypeList, ClassBodyDeclaration,
    ClassMemberDeclaration, InstanceInitializer, StaticInitializer, ConstructorDeclaration, FieldDeclaration,
    MethodDeclaration, FieldModifier, UnannType, VariableDeclaratorList, VariableDeclarator, VariableDeclaratorId,
    VariableInitializer, Expression, ArrayInitializer, UnannPrimitiveType, UnannReferenceType,
    UnannClassOrInterfaceType, UnannTypeVariable, UnannArrayType, UnannClassType, UnannInterfaceType, MethodModifier,
    MethodHeader, MethodBody, Result, MethodDeclarator, Throws, ReceiverParameter ,FormalParameterList, FormalParameter,
    VariableModifier, VariableArityParameter, ExceptionTypeList, ExceptionType, Block, ConstructorModifier,
    ConstructorDeclarator, ConstructorBody, SimpleTypeName, ExplicitConstructorInvocation, BlockStatements,
    ArgumentList, Primary, EnumBody, EnumConstantList, EnumBodyDeclarations, EnumConstant, EnumConstantModifier,
    NormalInterfaceDeclaration, AnnotationTypeDeclaration, InterfaceModifier, ExtendsInterfaces, InterfaceBody,
    InterfaceMemberDeclaration, ConstantDeclaration, InterfaceMethodDeclaration, ConstantModifier,
    InterfaceMethodModifier, AnnotationTypeBody, AnnotationTypeMemberDeclaration, AnnotationTypeElementDeclaration,
    AnnotationTypeElementModifier, DefaultValue, ElementValue, NormalAnnotation, MarkerAnnotation,
    SingleElementAnnotation, ElementValuePairList, ElementValuePair, ConditionalExpression,
    ElementValueArrayInitializer, ElementValueList, VariableInitializerList, BlockStatement,
    LocalVariableDeclarationStatement, Statement, LocalVariableDeclaration, LocalVariableType,
    StatementWithoutTrailingSubstatement, LabeledStatement, IfThenStatement, IfThenElseStatement, WhileStatement,
    ForStatement, StatementNoShortIf, LabeledStatementNoShortIf, IfThenElseStatementNoShortIf, WhileStatementNoShortIf,
    ForStatementNoShortIf, EmptyStatement, ExpressionStatement, AssertStatement, SwitchStatement, DoStatement,
    BreakStatement, ContinueStatement, ReturnStatement, SynchronizedStatement, ThrowStatement, TryStatement,
    YieldStatement, StatementExpression, Assignment, PreIncrementExpression, PreDecrementExpression,
    PostIncrementExpression, PostDecrementExpression, MethodInvocation, ClassInstanceCreationExpression, SwitchBlock,
    SwitchRule, SwitchBlockStatementGroup, SwitchLabel, CaseConstant, BasicForStatement, EnhancedForStatement,
    BasicForStatementNoShortIf, EnhancedForStatementNoShortIf, ForInit, ForUpdate, StatementExpressionList, Catches,
    Finally, TryWithResourcesStatement, CatchClause, CatchFormalParameter, CatchType, ResourceSpecification,
    ResourceList, Resource, VariableAccess, PrimaryNoNewArray, ArrayCreationExpression, ClassLiteral, FieldAccess,
    ArrayAccess, MethodReference, UnqualifiedClassInstanceCreationExpression, ClassOrInterfaceTypeToInstantiate,
    TypeArgumentsOrDiamond, DimExprs, DimExpr, LambdaExpression, AssignmentExpression, LambdaParameters, LambdaBody,
    LambdaParameterList, LambdaParameter, LambdaParameterType, LeftHandSide, AssignmentOperator,
    ConditionalOrExpression, ConditionalAndExpression, InclusiveOrExpression, ExclusiveOrExpression, AndExpression,
    EqualityExpression, RelationalExpression, ShiftExpression, AdditiveExpression, MultiplicativeExpression,
    UnaryExpression, UnaryExpressionNotPlusMinus, PostfixExpression, CastExpression, SwitchExpression,
    ConstantExpression, PrimitiveType1}

    static EBNF grammar() {
        ebnf {
            use(EBNFExtension) {
                Identifier >> IdentifierChars

                IdentifierChars >> JavaLetter & { JavaLetterOrDigit }

                JavaLetter >> ~"[A-Za-z]"

                JavaLetterOrDigit >> ~"[A-Za-z0-9_]"

                TypeIdentifier >> Identifier

                UnqualifiedMethodIdentifier >> Identifier

                Literal >> IntegerLiteral
                        | FloatingPointLiteral
                        | BooleanLiteral
                        | CharacterLiteral
                        | StringLiteral
                        | NullLiteral

                Type >> PrimitiveType | ReferenceType

                PrimitiveType >> { Annotation } & NumericType | { Annotation } & "boolean"

                NumericType >> IntegralType | FloatingPointType

                IntegralType >> "byte" | "short" | "int" | "long" | "char"

                FloatingPointType >> "float" | "double"

                ReferenceType >> ClassOrInterfaceType | TypeVariable | ArrayType

                ClassOrInterfaceType >> ClassType | InterfaceType

                ClassType >> { Annotation } & TypeIdentifier & [TypeArguments]
                        | PackageName & "." & { Annotation } & TypeIdentifier & [TypeArguments]
                        | ClassOrInterfaceType & "." & { Annotation } & TypeIdentifier & [TypeArguments]

                InterfaceType >> ClassType

                TypeVariable >> { Annotation } & TypeIdentifier

                ArrayType >> PrimitiveType & Dims
                        | ClassOrInterfaceType & Dims
                        | TypeVariable & Dims

                Dims >> { Annotation } & "[" & "]" & { { Annotation } & "[" & "]" }

                TypeParameter >> { TypeParameterModifier } & TypeIdentifier & [TypeBound]

                TypeParameterModifier >> Annotation

                TypeBound >> "extends" & TypeVariable | "extends" & ClassOrInterfaceType & { AdditionalBound }

                AdditionalBound >> "&" & InterfaceType

                TypeArguments >> "<" & TypeArgumentList & ">"

                TypeArgumentList >> TypeArgument & { "," & TypeArgument }

                TypeArgument >> ReferenceType | Wildcard

                Wildcard >> { Annotation } & "?" & [WildcardBounds]

                WildcardBounds >> "extends" & ReferenceType
                        | "super" & ReferenceType

                ModuleName >> Identifier
                        | ModuleName & "." & Identifier

                PackageName >> Identifier
                        | PackageName & "." & Identifier

                TypeName >> TypeIdentifier
                        | PackageOrTypeName & "." & TypeIdentifier

                ExpressionName >> Identifier
                        | AmbiguousName & "." & Identifier

                MethodName >> UnqualifiedMethodIdentifier

                PackageOrTypeName >> Identifier
                        | PackageOrTypeName & "." & Identifier

                AmbiguousName >> Identifier
                        | AmbiguousName & "." & Identifier

                CompilationUnit >> OrdinaryCompilationUnit
                        | ModularCompilationUnit

                OrdinaryCompilationUnit >> [PackageDeclaration] & { ImportDeclaration } & { TypeDeclaration }

                ModularCompilationUnit >> { ImportDeclaration } & ModuleDeclaration

                PackageDeclaration >> { PackageModifier } & "package" & Identifier & { "." & Identifier } & ";"

                PackageModifier >> Annotation

                ImportDeclaration >> SingleTypeImportDeclaration
                        | TypeImportOnDemandDeclaration
                        | SingleStaticImportDeclaration
                        | StaticImportOnDemandDeclaration

                SingleTypeImportDeclaration >> "import" & TypeName & ";"

                TypeImportOnDemandDeclaration >> "import" & PackageOrTypeName & "." & "*" & ";"

                SingleStaticImportDeclaration >> "import" & "static" & TypeName & "." & Identifier & ";"

                StaticImportOnDemandDeclaration >> "import" & "static" & TypeName & "." & "*" & ";"

                TypeDeclaration >> ClassDeclaration
                        | InterfaceDeclaration
                        | ";"

                ModuleDeclaration >> { Annotation } & ["open"] & "module" & Identifier & { "." & Identifier }
                        & "{" & { ModuleDirective } & "}"

                ModuleDirective >> "requires" & { RequiresModifier } & ModuleName & ";"
                        | "exports" & PackageName & ["to" & ModuleName & { "," & ModuleName }] & ";"
                        | "opens" & PackageName & ["to" & ModuleName & { "," & ModuleName }] & ";"
                        | "uses" & TypeName & ";"
                        | "provides" & TypeName & "with" & TypeName & { "," & TypeName } & ";"

                RequiresModifier >> "transitive" | "static"

                ClassDeclaration >> NormalClassDeclaration
                        | EnumDeclaration

                NormalClassDeclaration >> { ClassModifier } & "class" & TypeIdentifier & [TypeParameters] & [Superclass]
                        & [Superinterfaces] & ClassBody

                ClassModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static"
                        | "final" | "strictfp"

                TypeParameters >> "<" & TypeParameterList & ">"

                TypeParameterList >> TypeParameter & { "," & TypeParameter }

                Superclass >> "extends" & ClassType

                Superinterfaces >> "implements" & InterfaceTypeList

                InterfaceTypeList >> InterfaceType & { "," & InterfaceType }

                ClassBody >> "{" & { ClassBodyDeclaration } & "}"

                ClassBodyDeclaration >> ClassMemberDeclaration
                        | InstanceInitializer
                        | StaticInitializer
                        | ConstructorDeclaration

                ClassMemberDeclaration >> FieldDeclaration
                        | MethodDeclaration
                        | ClassDeclaration
                        | InterfaceDeclaration
                        | ";"

                FieldDeclaration >> { FieldModifier } & UnannType & VariableDeclaratorList & ";"

                FieldModifier >> Annotation | "public" | "protected" | "private" | "static" | "final"
                        | "transient" | "volatile"

                VariableDeclaratorList >> VariableDeclarator & { "," & VariableDeclarator }

                VariableDeclarator >> VariableDeclaratorId & ["=" & VariableInitializer]

                VariableDeclaratorId >> Identifier & [Dims]

                VariableInitializer >> Expression
                        | ArrayInitializer

                UnannType >> UnannPrimitiveType
                        | UnannReferenceType

                UnannPrimitiveType >> NumericType
                        | "boolean"

                UnannReferenceType >> UnannClassOrInterfaceType
                        | UnannTypeVariable
                        | UnannArrayType

                UnannClassOrInterfaceType >> UnannClassType
                        | UnannInterfaceType

                UnannClassType >> TypeIdentifier & [TypeArguments]
                        | PackageName & "." & { Annotation } & TypeIdentifier & [TypeArguments]
                        | UnannClassOrInterfaceType & "." & { Annotation } & TypeIdentifier & [TypeArguments]

                UnannInterfaceType >> UnannClassType

                UnannTypeVariable >> TypeIdentifier

                UnannArrayType >> UnannPrimitiveType & Dims
                        | UnannClassOrInterfaceType & Dims
                        | UnannTypeVariable & Dims

                MethodDeclaration >> { MethodModifier } & MethodHeader & MethodBody

                MethodModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static" | "final"
                        | "synchronized" | "native" | "strictfp"

                MethodHeader >> Result & MethodDeclarator & [Throws]
                        | TypeParameters & { Annotation } & Result & MethodDeclarator & [Throws]

                Result >> UnannType
                        | "void"

                MethodDeclarator >> Identifier & "(" & [ReceiverParameter & ","] & [FormalParameterList] & ")" & [Dims]

                ReceiverParameter >> { Annotation } & UnannType & [Identifier & "."] & "this"

                FormalParameterList >> FormalParameter & { "," & FormalParameter }

                FormalParameter >> { VariableModifier } & UnannType & VariableDeclaratorId
                        | VariableArityParameter

                VariableArityParameter >> { VariableModifier } & UnannType & { Annotation } & "..." & Identifier

                VariableModifier >> Annotation
                        | "final"

                Throws >> "throws" & ExceptionTypeList

                ExceptionTypeList >> ExceptionType & { "," & ExceptionType }

                ExceptionType >> ClassType
                        | TypeVariable

                MethodBody >> Block
                        | ";"

                InstanceInitializer >> Block

                StaticInitializer >> "static" & Block

                ConstructorDeclaration >> { ConstructorModifier } & ConstructorDeclarator & [Throws] & ConstructorBody

                ConstructorModifier >> Annotation | "public" | "protected" | "private"

                ConstructorDeclarator >> [TypeParameters] & SimpleTypeName & "(" & [ReceiverParameter & ","]
                        & [FormalParameterList] & ")"

                SimpleTypeName >> TypeIdentifier

                ConstructorBody >> "{" & [ExplicitConstructorInvocation] & [BlockStatements] & "}"

                ExplicitConstructorInvocation >> [TypeArguments] & "this" & "(" & [ArgumentList] & ")" & ";"
                        | [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"
                        | ExpressionName & "." & [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"
                        | Primary & "." & [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"

                EnumDeclaration >> { ClassModifier } & "enum" & TypeIdentifier & [Superinterfaces] & EnumBody

                EnumBody >> "{" & [EnumConstantList] & [","] & [EnumBodyDeclarations] & "}"

                EnumConstantList >> EnumConstant & { "," & EnumConstant }

                EnumConstant >> { EnumConstantModifier } & Identifier & ["(" & [ArgumentList] & ")"] & [ClassBody]

                EnumConstantModifier >> Annotation

                EnumBodyDeclarations >> ";" & { ClassBodyDeclaration }

                InterfaceDeclaration >> NormalInterfaceDeclaration
                        | AnnotationTypeDeclaration

                NormalInterfaceDeclaration >> { InterfaceModifier } & "interface" & TypeIdentifier & [TypeParameters]
                        & [ExtendsInterfaces] & InterfaceBody

                InterfaceModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static" | "strictfp"

                ExtendsInterfaces >> "extends" & InterfaceTypeList

                InterfaceBody >> "{" & { InterfaceMemberDeclaration } & "}"

                InterfaceMemberDeclaration >> ConstantDeclaration
                        | InterfaceMethodDeclaration
                        | ClassDeclaration
                        | InterfaceDeclaration
                        | ";"

                ConstantDeclaration >> { ConstantModifier } & UnannType & VariableDeclaratorList & ";"

                ConstantModifier >> Annotation | "public" | "static" | "final"

                InterfaceMethodDeclaration >> { InterfaceMethodModifier } & MethodHeader & MethodBody

                InterfaceMethodModifier >> Annotation | "public" | "private" | "abstract" | "default" | "static" | "strictfp"

                AnnotationTypeDeclaration >> { InterfaceModifier } & "@" & "interface" & TypeIdentifier & AnnotationTypeBody

                AnnotationTypeBody >> "{" & { AnnotationTypeMemberDeclaration } & "}"

                AnnotationTypeMemberDeclaration >> AnnotationTypeElementDeclaration
                        | ConstantDeclaration
                        | ClassDeclaration
                        | InterfaceDeclaration
                        | ";"

                AnnotationTypeElementDeclaration >> { AnnotationTypeElementModifier } & UnannType & Identifier & "(" & ")" & [Dims]
                        & [DefaultValue] & ";"

                AnnotationTypeElementModifier >> Annotation | "public" | "abstract"

                DefaultValue >> "default" & ElementValue

                Annotation >> NormalAnnotation
                        | MarkerAnnotation
                        | SingleElementAnnotation

                NormalAnnotation >> "@" & TypeName & "(" & [ElementValuePairList] & ")"

                ElementValuePairList >> ElementValuePair & { "," & ElementValuePair }

                ElementValuePair >> Identifier & "=" & ElementValue

                ElementValue >> ConditionalExpression
                        | ElementValueArrayInitializer
                        | Annotation

                ElementValueArrayInitializer >> "{" & [ElementValueList] & [","] & "}"

                ElementValueList >> ElementValue & { "," & ElementValue }

                MarkerAnnotation >> "@" & TypeName

                SingleElementAnnotation >> "@" & TypeName & "(" & ElementValue & ")"

                ArrayInitializer >> "{" & [VariableInitializerList] & [","] & "}"

                VariableInitializerList >> VariableInitializer & { "," & VariableInitializer }

                Block >> "{" & [BlockStatements] & "}"

                BlockStatements >> BlockStatement & { BlockStatement }

                BlockStatement >> LocalVariableDeclarationStatement
                        | ClassDeclaration
                        | Statement

                LocalVariableDeclarationStatement >> LocalVariableDeclaration & ";"

                LocalVariableDeclaration >> { VariableModifier } & LocalVariableType & VariableDeclaratorList

                LocalVariableType >> UnannType
                        | "var"

                Statement >> StatementWithoutTrailingSubstatement
                        | LabeledStatement
                        | IfThenStatement
                        | IfThenElseStatement
                        | WhileStatement
                        | ForStatement

                StatementNoShortIf >> StatementWithoutTrailingSubstatement
                        | LabeledStatementNoShortIf
                        | IfThenElseStatementNoShortIf
                        | WhileStatementNoShortIf
                        | ForStatementNoShortIf

                StatementWithoutTrailingSubstatement >> Block
                        | EmptyStatement
                        | ExpressionStatement
                        | AssertStatement
                        | SwitchStatement
                        | DoStatement
                        | BreakStatement
                        | ContinueStatement
                        | ReturnStatement
                        | SynchronizedStatement
                        | ThrowStatement
                        | TryStatement
                        | YieldStatement

                EmptyStatement >> ";"

                LabeledStatement >> Identifier & ":" & Statement

                LabeledStatementNoShortIf >> Identifier & ":" & StatementNoShortIf

                ExpressionStatement >> StatementExpression & ";"

                StatementExpression >> Assignment
                        | PreIncrementExpression
                        | PreDecrementExpression
                        | PostIncrementExpression
                        | PostDecrementExpression
                        | MethodInvocation
                        | ClassInstanceCreationExpression

                IfThenStatement >> "if" & "(" & Expression & ")" & Statement

                IfThenElseStatement >> "if" & "(" & Expression & ")" & StatementNoShortIf & "else" & Statement

                IfThenElseStatementNoShortIf >> "if" & "(" & Expression & ")" & StatementNoShortIf & "else" & StatementNoShortIf

                AssertStatement >> "assert" & Expression & ";"
                        | "assert" & Expression & ":" & Expression & ";"

                SwitchStatement >> "switch" & "(" & Expression & ")" & SwitchBlock

                SwitchBlock >> "{" & SwitchRule & { SwitchRule } & "}"
                        | "{" & { SwitchBlockStatementGroup } & { SwitchLabel & ":" } & "}"

                SwitchRule >> SwitchLabel & "->" & Expression & ";"
                        | SwitchLabel & "->" & Block
                        | SwitchLabel & "->" & ThrowStatement

                SwitchBlockStatementGroup >> SwitchLabel & ":" & { SwitchLabel & ":" } & BlockStatements

                SwitchLabel >> "case" & CaseConstant & { "," & CaseConstant }
                        | "default"

                CaseConstant >> ConditionalExpression

                WhileStatement >> "while" & "(" & Expression & ")" & Statement

                WhileStatementNoShortIf >> "while" & "(" & Expression & ")" & StatementNoShortIf

                DoStatement >> "do" & Statement & "while" & "(" & Expression & ")" & ";"

                ForStatement >> BasicForStatement
                        | EnhancedForStatement

                ForStatementNoShortIf >> BasicForStatementNoShortIf
                        | EnhancedForStatementNoShortIf

                BasicForStatement >> "for" & "(" & [ForInit] & ";" & [Expression] & ";" & [ForUpdate] & ")" & Statement

                BasicForStatementNoShortIf >> "for" & "(" & [ForInit] & ";" & [Expression] & ";" & [ForUpdate]
                        & ")" & StatementNoShortIf

                ForInit >> StatementExpressionList
                        | LocalVariableDeclaration

                ForUpdate >> StatementExpressionList

                StatementExpressionList >> StatementExpression & { "," & StatementExpression }

                EnhancedForStatement >> "for" & "(" & { VariableModifier } & LocalVariableType & VariableDeclaratorId & ":"
                        & Expression & ")" & Statement

                EnhancedForStatementNoShortIf >> "for" & "(" & { VariableModifier } & LocalVariableType & VariableDeclaratorId & ":"
                        & Expression & ")" & StatementNoShortIf

                BreakStatement >> "break" & [Identifier] & ";"

                YieldStatement >> "yield" & Expression & ";"

                ContinueStatement >> "continue" & [Identifier] & ";"

                ReturnStatement >> "return" & [Expression] & ";"

                ThrowStatement >> "throw" & Expression & ";"

                SynchronizedStatement >> "synchronized" & "(" & Expression & ")" & Block

                TryStatement >> "try" & Block & Catches
                        | "try" & Block & [Catches] & Finally
                        | TryWithResourcesStatement

                Catches >> CatchClause & { CatchClause }

                CatchClause >> "catch" & "(" & CatchFormalParameter & ")" & Block

                CatchFormalParameter >> { VariableModifier } & CatchType & VariableDeclaratorId

                CatchType >> UnannClassType & { "|" & ClassType }

                Finally >> "finally" & Block

                TryWithResourcesStatement >> "try" & ResourceSpecification & Block & [Catches] & [Finally]

                ResourceSpecification >> "(" & ResourceList & [";"] & ")"

                ResourceList >> Resource & { ";" & Resource }

                Resource >> { VariableModifier } & LocalVariableType & Identifier & "=" & Expression
                        | VariableAccess

                Primary >> PrimaryNoNewArray
                        | ArrayCreationExpression

                PrimaryNoNewArray >> Literal
                        | ClassLiteral
                        | "this"
                        | TypeName & "." & "this"
                        | "(" & Expression & ")"
                        | ClassInstanceCreationExpression
                        | FieldAccess
                        | ArrayAccess
                        | MethodInvocation
                        | MethodReference

                ClassLiteral >> TypeName & { "[" & "]" } & "." & "class"
                        | NumericType & { "[" & "]" } & "." & "class"
                        | "boolean" & { "[" & "]" } & "." & "class"
                        | "void" & "." & "class"

                ClassInstanceCreationExpression >> UnqualifiedClassInstanceCreationExpression
                        | ExpressionName & "." & UnqualifiedClassInstanceCreationExpression
                        | Primary & "." & UnqualifiedClassInstanceCreationExpression

                UnqualifiedClassInstanceCreationExpression >> "new" & [TypeArguments] & ClassOrInterfaceTypeToInstantiate & "("
                        & [ArgumentList] & ")" & [ClassBody]

                ClassOrInterfaceTypeToInstantiate >> { Annotation } & Identifier & {
                    "." & { Annotation }
                            & Identifier
                } & [TypeArgumentsOrDiamond]

                TypeArgumentsOrDiamond >> TypeArguments
                        | "<>"

                FieldAccess >> Primary & "." & Identifier
                        | "super" & "." & Identifier
                        | TypeName & "." & "super" & "." & Identifier

                ArrayAccess >> ExpressionName & "[" & Expression & "]"
                        | PrimaryNoNewArray & "[" & Expression & "]"

                MethodInvocation >> MethodName & "(" & [ArgumentList] & ")"
                        | TypeName & "." & [TypeArguments] & Identifier & "(" & [ArgumentList] & ")"
                        | ExpressionName & "." & [TypeArguments] & Identifier & "(" & [ArgumentList] & ")"
                        | Primary & "." & [TypeArguments] & Identifier & "(" & [ArgumentList] & ")"
                        | "super" & "." & [TypeArguments] & Identifier & "(" & [ArgumentList] & ")"
                        | TypeName & "." & "super" & "." & [TypeArguments] & Identifier & "(" & [ArgumentList] & ")"

                ArgumentList >> Expression & { "," & Expression }

                MethodReference >> ExpressionName & "::" & [TypeArguments] & Identifier
                        | Primary & "::" & [TypeArguments] & Identifier
                        | ReferenceType & "::" & [TypeArguments] & Identifier
                        | "super" & "::" & [TypeArguments] & Identifier
                        | TypeName & "." & "super" & "::" & [TypeArguments] & Identifier
                        | ClassType & "::" & [TypeArguments] & "new"
                        | ArrayType & "::" & "new"

                ArrayCreationExpression >> "new" & PrimitiveType & DimExprs & [Dims]
                        | "new" & ClassOrInterfaceType & DimExprs & [Dims]
                        | "new" & PrimitiveType & Dims & ArrayInitializer
                        | "new" & ClassOrInterfaceType & Dims & ArrayInitializer

                DimExprs >> DimExpr & { DimExpr }

                DimExpr >> { Annotation } & "[" & Expression & "]"

                Expression >> LambdaExpression
                        | AssignmentExpression

                LambdaExpression >> LambdaParameters & "->" & LambdaBody

                LambdaParameters >> "(" & [LambdaParameterList] & ")"
                        | Identifier

                LambdaParameterList >> LambdaParameter & { "," & LambdaParameter }
                        | Identifier & { "," & Identifier }

                LambdaParameter >> { VariableModifier } & LambdaParameterType & VariableDeclaratorId
                        | VariableArityParameter

                LambdaParameterType >> UnannType
                        | "var"

                LambdaBody >> Expression
                        | Block

                AssignmentExpression >> ConditionalExpression
                        | Assignment

                Assignment >> LeftHandSide & AssignmentOperator & Expression

                LeftHandSide >> ExpressionName
                        | FieldAccess
                        | ArrayAccess

                AssignmentOperator >> "=" | "*=" | "/=" | "%=" | "+=" | "-=" | "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|="

                ConditionalExpression >> ConditionalOrExpression
                        | ConditionalOrExpression & "?" & Expression & ":" & ConditionalExpression
                        | ConditionalOrExpression & "?" & Expression & ":" & LambdaExpression

                ConditionalOrExpression >> ConditionalAndExpression
                        | ConditionalOrExpression & "||" & ConditionalAndExpression

                ConditionalAndExpression >> InclusiveOrExpression
                        | ConditionalAndExpression & "&&" & InclusiveOrExpression

                InclusiveOrExpression >> ExclusiveOrExpression
                        | InclusiveOrExpression & "|" & ExclusiveOrExpression

                ExclusiveOrExpression >> AndExpression
                        | ExclusiveOrExpression & "^" & AndExpression

                AndExpression >> EqualityExpression
                        | AndExpression & "&" & EqualityExpression

                EqualityExpression >> RelationalExpression
                        | EqualityExpression & "==" & RelationalExpression
                        | EqualityExpression & "!=" & RelationalExpression

                RelationalExpression >> ShiftExpression
                        | RelationalExpression & "<" & ShiftExpression
                        | RelationalExpression & ">" & ShiftExpression
                        | RelationalExpression & "<=" & ShiftExpression
                        | RelationalExpression & ">=" & ShiftExpression
                        | RelationalExpression & "instanceof" & ReferenceType

                ShiftExpression >> AdditiveExpression
                        | ShiftExpression & "<<" & AdditiveExpression
                        | ShiftExpression & ">>" & AdditiveExpression
                        | ShiftExpression & ">>>" & AdditiveExpression

                AdditiveExpression >> MultiplicativeExpression
                        | AdditiveExpression & "+" & MultiplicativeExpression
                        | AdditiveExpression & "-" & MultiplicativeExpression

                MultiplicativeExpression >> UnaryExpression
                        | MultiplicativeExpression & "*" & UnaryExpression
                        | MultiplicativeExpression & "/" & UnaryExpression
                        | MultiplicativeExpression & "%" & UnaryExpression

                UnaryExpression >> PreIncrementExpression
                        | PreDecrementExpression
                        | "+" & UnaryExpression
                        | "-" & UnaryExpression
                        | UnaryExpressionNotPlusMinus

                PreIncrementExpression >> "++" & UnaryExpression

                PreDecrementExpression >> "--" & UnaryExpression

                UnaryExpressionNotPlusMinus >> PostfixExpression
                        | "~" & UnaryExpression
                        | "!" & UnaryExpression
                        | CastExpression
                        | SwitchExpression

                PostfixExpression >> Primary
                        | ExpressionName
                        | PostIncrementExpression
                        | PostDecrementExpression

                PostIncrementExpression >> PostfixExpression & "++"

                PostDecrementExpression >> PostfixExpression & "--"

                CastExpression >> "(" & PrimitiveType & ")" & UnaryExpression
                        | "(" & ReferenceType & { AdditionalBound } & ")" & UnaryExpressionNotPlusMinus
                        | "(" & ReferenceType & { AdditionalBound } & ")" & LambdaExpression

                SwitchExpression >> "switch" & "(" & Expression & ")" & SwitchBlock

                ConstantExpression >> Expression
            }
        }
    }

    static void main(String[] args) {
        EBNF java = grammar()

        println(java.cycles().join("\n"))
        println()
        println(java.entryPoints())



//
//
//        println(java.entryPoints())
//        Set<List<Variable>> x = java.cycles()
//        List<List<Variable>> y = x.asList()
//
//        println(y.find{it == [Type, CompilationUnit, ConstantExpression]}? 1 : 2)
//
//
//        println(y.take(10).join("\n"))
    }
}
