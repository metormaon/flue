package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.ebnf.element.Variable

import static il.ac.openu.flue.JavaEbnf.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**
 * @author Noam Rotem
 */
class JavaEbnfAnnotated {
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

    static void main(String[] args) {
        EBNF java = ebnf {
            //enums should be flyweights

            //If:
            //A -> B
            //A -> C & D | E
            //Then:
            //A -> (B) | (C & D | E)
            //And finally:
            //A -> B | C & D | E

            //Left is an interface. Right is class (or interface) that implements (also) left.
            Identifier >> IdentifierChars
            TypeIdentifier >> Identifier
            InterfaceType >> ClassType
            TypeParameterModifier >> Annotation
            UnqualifiedMethodIdentifier >> Identifier
            MethodName >> UnqualifiedMethodIdentifier
            PackageModifier >> Annotation
            UnannInterfaceType >> UnannClassType
            UnannTypeVariable >> TypeIdentifier
            SimpleTypeName >> TypeIdentifier
            EnumConstantModifier >> Annotation
            CaseConstant >> ConditionalExpression
            ForUpdate >> StatementExpressionList
            ConstantExpression >> Expression

            //Removing non-informative tokens, then: left is an interface. Right is class (or interface) that
            // implements (also) left.
            AdditionalBound >> "&" & InterfaceType
            TypeArguments >> "<" & TypeArgumentList & ">"
            SingleTypeImportDeclaration >> "import" & TypeName & ";"
            TypeImportOnDemandDeclaration >> "import" & PackageOrTypeName & "." & "*" & ";"
            StaticImportOnDemandDeclaration >> "import" & "static" & TypeName & "." & "*" & ";"
            TypeParameters >> "<" & TypeParameterList & ">"
            Superclass >> "extends" & ClassType
            Superinterfaces >> "implements" & InterfaceTypeList
            Throws >> "throws" & ExceptionTypeList
            InstanceInitializer >> Block
            StaticInitializer >> "static" & Block
            ExtendsInterfaces >> "extends" & InterfaceTypeList
            DefaultValue >> "default" & ElementValue
            MarkerAnnotation >> "@" & TypeName
            LocalVariableDeclarationStatement >> LocalVariableDeclaration & ";"
            EmptyStatement >> ";"
            ExpressionStatement >> StatementExpression & ";"
            YieldStatement >> "yield" & Expression & ";"
            ThrowStatement >> "throw" & Expression & ";"
            Finally >> "finally" & Block
            PreIncrementExpression >> "++" & UnaryExpression
            PreDecrementExpression >> "--" & UnaryExpression
            PostIncrementExpression >> PostfixExpression & "++"
            PostDecrementExpression >> PostfixExpression & "--"

            //Class with A string member
            JavaLetter >> "[A-Za-z]"
            JavaLetterOrDigit >> "[A-Za-z0-9_]"

            //Left is interface. All right options implement left.
            Literal >> IntegerLiteral
                    | FloatingPointLiteral
                    | BooleanLiteral
                    | CharacterLiteral
                    | StringLiteral
                    | NullLiteral
            Type >> PrimitiveType | ReferenceType
            NumericType >> IntegralType | FloatingPointType
            ReferenceType >> ClassOrInterfaceType | TypeVariable | ArrayType
            ClassOrInterfaceType >> ClassType | InterfaceType
            TypeArgument >> ReferenceType | Wildcard
            CompilationUnit >> OrdinaryCompilationUnit
                    | ModularCompilationUnit
            ImportDeclaration >> SingleTypeImportDeclaration
                    | TypeImportOnDemandDeclaration
                    | SingleStaticImportDeclaration
                    | StaticImportOnDemandDeclaration
            ClassDeclaration >> NormalClassDeclaration
                    | EnumDeclaration
            ClassBodyDeclaration >> ClassMemberDeclaration
                    | InstanceInitializer
                    | StaticInitializer
                    | ConstructorDeclaration
            VariableInitializer >> Expression
                    | ArrayInitializer
            UnannType >> UnannPrimitiveType
                    | UnannReferenceType
            UnannReferenceType >> UnannClassOrInterfaceType
                    | UnannTypeVariable
                    | UnannArrayType
            UnannClassOrInterfaceType >> UnannClassType
                    | UnannInterfaceType
            Annotation >> NormalAnnotation
                    | MarkerAnnotation
                    | SingleElementAnnotation
            ElementValue >> ConditionalExpression
                    | ElementValueArrayInitializer
                    | Annotation
            BlockStatement >> LocalVariableDeclarationStatement
                    | ClassDeclaration
                    | Statement
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
            StatementExpression >> Assignment
                    | PreIncrementExpression
                    | PreDecrementExpression
                    | PostIncrementExpression
                    | PostDecrementExpression
                    | MethodInvocation
                    | ClassInstanceCreationExpression
            ForStatement >> BasicForStatement
                    | EnhancedForStatement
            ForStatementNoShortIf >> BasicForStatementNoShortIf
                    | EnhancedForStatementNoShortIf
            ForInit >> StatementExpressionList
                    | LocalVariableDeclaration
            Primary >> PrimaryNoNewArray
                    | ArrayCreationExpression
            Expression >> LambdaExpression
                    | AssignmentExpression
            LambdaBody >> Expression
                    | Block
            AssignmentExpression >> ConditionalExpression
                    | Assignment
            LeftHandSide >> ExpressionName
                    | FieldAccess
                    | ArrayAccess
            PostfixExpression >> Primary
                    | ExpressionName
                    | PostIncrementExpression
                    | PostDecrementExpression
            ExceptionType >> ClassType
                    | TypeVariable
            InterfaceDeclaration >> NormalInterfaceDeclaration
                    | AnnotationTypeDeclaration

            //Removing non-informative tokens, then: left is interface. All right options implement left.
            UnaryExpression >> PreIncrementExpression
                    | PreDecrementExpression
                    | "+" & UnaryExpression
                    | "-" & UnaryExpression
                    | UnaryExpressionNotPlusMinus
            UnaryExpressionNotPlusMinus >> PostfixExpression
                    | "~" & UnaryExpression
                    | "!" & UnaryExpression
                    | CastExpression
                    | SwitchExpression

            //Left is interface. All right options implement left. All occurrences of left in right sides become
            // optional
            TypeDeclaration >> ClassDeclaration
                    | InterfaceDeclaration
                    | ";"
            ClassMemberDeclaration >> FieldDeclaration
                    | MethodDeclaration
                    | ClassDeclaration
                    | InterfaceDeclaration
                    | ";"
            UnannPrimitiveType >> NumericType
                    | "boolean"
            Result >> UnannType
                    | "void"
            LocalVariableType >> UnannType
                    | "var"
            TypeArgumentsOrDiamond >> TypeArguments
                    | "<>"
            LambdaParameterType >> UnannType
                    | "var"
            VariableModifier >> Annotation
                    | "final"
            MethodBody >> Block
                    | ";"
            InterfaceMemberDeclaration >> ConstantDeclaration
                    | InterfaceMethodDeclaration
                    | ClassDeclaration
                    | InterfaceDeclaration
                    | ";"
            AnnotationTypeMemberDeclaration >> AnnotationTypeElementDeclaration
                    | ConstantDeclaration
                    | ClassDeclaration
                    | InterfaceDeclaration
                    | ";"

            //Removing non-informative tokens, then: Left is interface. All right options implement left. All
            // occurrences of left in right sides become optional
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

            //Right becomes inner enum of left. Left is class with enum field.
            IntegralType >> "byte" | "short" | "int" | "long" | "char"
            FloatingPointType >> "float" | "double"
            AssignmentOperator >> "=" | "*=" | "/=" | "%=" | "+=" | "-=" | "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|="
            RequiresModifier >> "transitive" | "static"

            //left is interface inherited by class in right, and by a class that has an enum as a field
            ClassModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static"
                    | "final" | "strictfp"
            FieldModifier >> Annotation | "public" | "protected" | "private" | "static" | "final"
                    | "transient" | "volatile"
            MethodModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static" | "final"
                    | "synchronized" | "native" | "strictfp"
            ConstructorModifier >> Annotation | "public" | "protected" | "private"
            InterfaceModifier >> Annotation | "public" | "protected" | "private" | "abstract" | "static" | "strictfp"
            ConstantModifier >> Annotation | "public" | "static" | "final"
            InterfaceMethodModifier >> Annotation | "public" | "private" | "abstract" | "default" | "static" | "strictfp"
            AnnotationTypeElementModifier >> Annotation | "public" | "abstract"

            //Left is a class with fields for all non terminals in right
            Assignment >> LeftHandSide & AssignmentOperator & Expression

            //Remove tokens, then: Left is a class with fields for all non terminals in right
            SingleStaticImportDeclaration >> "import" & "static" & TypeName & "." & Identifier & ";"
            ElementValuePair >> Identifier & "=" & ElementValue
            SingleElementAnnotation >> "@" & TypeName & "(" & ElementValue & ")"
            LabeledStatement >> Identifier & ":" & Statement
            LabeledStatementNoShortIf >> Identifier & ":" & StatementNoShortIf
            IfThenStatement >> "if" & "(" & Expression & ")" & Statement
            IfThenElseStatement >> "if" & "(" & Expression & ")" & StatementNoShortIf & "else" & Statement
            IfThenElseStatementNoShortIf >> "if" & "(" & Expression & ")" & StatementNoShortIf & "else" & StatementNoShortIf
            SwitchStatement >> "switch" & "(" & Expression & ")" & SwitchBlock
            WhileStatement >> "while" & "(" & Expression & ")" & Statement
            WhileStatementNoShortIf >> "while" & "(" & Expression & ")" & StatementNoShortIf
            DoStatement >> "do" & Statement & "while" & "(" & Expression & ")" & ";"
            SynchronizedStatement >> "synchronized" & "(" & Expression & ")" & Block
            CatchClause >> "catch" & "(" & CatchFormalParameter & ")" & Block
            LambdaExpression >> LambdaParameters & "->" & LambdaBody
            SwitchExpression >> "switch" & "(" & Expression & ")" & SwitchBlock

            //After potential token removal: Left is a class with fields for all non terminals in right
            // (some are lists, some are optional)
            IdentifierChars >> JavaLetter & { JavaLetterOrDigit }
            TypeVariable >> { Annotation } & TypeIdentifier
            ModularCompilationUnit >> {ImportDeclaration} & ModuleDeclaration
            TypeParameter >> { TypeParameterModifier } & TypeIdentifier & [TypeBound]
            NormalClassDeclaration >> {ClassModifier} & "class" & TypeIdentifier & [TypeParameters] & [Superclass]
                    & [Superinterfaces] & ClassBody
            FieldDeclaration >> {FieldModifier} & UnannType & VariableDeclaratorList & ";"
            VariableDeclarator >> VariableDeclaratorId & ["=" & VariableInitializer]
            VariableDeclaratorId >> Identifier & [Dims]
            MethodDeclaration >> {MethodModifier} & MethodHeader & MethodBody
            MethodDeclarator >> Identifier & "(" & [ReceiverParameter & ","] & [FormalParameterList] & ")" & [Dims]
            ReceiverParameter >> {Annotation} & UnannType & [Identifier & "."] & "this"
            VariableArityParameter >> {VariableModifier} & UnannType & {Annotation} & "..." & Identifier
            Wildcard >> { Annotation } & "?" & [WildcardBounds]
            OrdinaryCompilationUnit >> [PackageDeclaration] & {ImportDeclaration} & {TypeDeclaration}
            ClassBody >> "{" & {ClassBodyDeclaration} & "}"
            ConstructorDeclaration >> {ConstructorModifier} & ConstructorDeclarator & [Throws] & ConstructorBody
            ConstructorDeclarator >> [TypeParameters] & SimpleTypeName & "(" & [ReceiverParameter & ","]
                    & [FormalParameterList] & ")"
            ConstructorBody >> "{" & [ExplicitConstructorInvocation] & [BlockStatements] & "}"
            EnumDeclaration >> {ClassModifier} & "enum" & TypeIdentifier & [Superinterfaces] & EnumBody
            EnumBodyDeclarations >> ";" & {ClassBodyDeclaration}
            NormalInterfaceDeclaration >> {InterfaceModifier} & "interface" & TypeIdentifier & [TypeParameters]
                    & [ExtendsInterfaces] & InterfaceBody
            InterfaceBody >> "{" & {InterfaceMemberDeclaration} & "}"
            ConstantDeclaration >> {ConstantModifier} & UnannType & VariableDeclaratorList & ";"
            InterfaceMethodDeclaration >> {InterfaceMethodModifier} & MethodHeader & MethodBody
            AnnotationTypeDeclaration >> {InterfaceModifier} & "@" & "interface" & TypeIdentifier & AnnotationTypeBody
            AnnotationTypeBody >> "{" & {AnnotationTypeMemberDeclaration} & "}"
            AnnotationTypeElementDeclaration >> {AnnotationTypeElementModifier} & UnannType & Identifier
                    & "(" & ")" & [Dims] & [DefaultValue] & ";"
            NormalAnnotation >> "@" & TypeName & "(" & [ElementValuePairList] & ")"
            Block >> "{" & [BlockStatements] & "}"
            BlockStatements >> BlockStatement & {BlockStatement}
            LocalVariableDeclaration >> {VariableModifier} & LocalVariableType & VariableDeclaratorList
            BasicForStatement >> "for" & "(" & [ForInit] & ";" & [Expression] & ";" & [ForUpdate] & ")" & Statement
            BasicForStatementNoShortIf >> "for" & "(" & [ForInit] & ";" & [Expression] & ";" & [ForUpdate]
                    & ")" & StatementNoShortIf
            StatementExpressionList >> StatementExpression & {"," & StatementExpression}
            EnhancedForStatement >> "for" & "(" & {VariableModifier} & LocalVariableType & VariableDeclaratorId & ":"
                    & Expression & ")" & Statement
            EnhancedForStatementNoShortIf >> "for" & "(" & {VariableModifier} & LocalVariableType & VariableDeclaratorId & ":"
                    & Expression & ")" & StatementNoShortIf
            BreakStatement >> "break" & [Identifier] & ";"
            ContinueStatement >> "continue" & [Identifier] & ";"
            ReturnStatement >> "return" & [Expression] & ";"
            CatchFormalParameter >> {VariableModifier} & CatchType & VariableDeclaratorId
            CatchType >> UnannClassType & {"|" & ClassType}
            TryWithResourcesStatement >> "try" & ResourceSpecification & Block & [Catches] & [Finally]
            UnqualifiedClassInstanceCreationExpression >> "new" & [TypeArguments] & ClassOrInterfaceTypeToInstantiate & "("
                    & [ArgumentList] & ")" & [ClassBody]
            DimExpr >> {Annotation} & "[" & Expression & "]"

            //Sequence with X x; List<X> lx;
            TypeArgumentList >> TypeArgument & { "," & TypeArgument }
            PackageDeclaration >> {PackageModifier} & "package" & Identifier & {"." & Identifier} & ";"
            TypeParameterList >> TypeParameter & {"," & TypeParameter}
            InterfaceTypeList >> InterfaceType & {"," & InterfaceType}
            VariableDeclaratorList >> VariableDeclarator & {"," & VariableDeclarator}
            FormalParameterList >> FormalParameter & {"," & FormalParameter}
            ExceptionTypeList >> ExceptionType & {"," & ExceptionType}
            EnumConstantList >> EnumConstant & {"," & EnumConstant}
            ElementValuePairList >> ElementValuePair & {"," & ElementValuePair}
            ElementValueList >> ElementValue & {"," & ElementValue}
            VariableInitializerList >> VariableInitializer & {"," & VariableInitializer}
            SwitchBlockStatementGroup >> SwitchLabel & ":" & {SwitchLabel & ":"} & BlockStatements
            Catches >> CatchClause & {CatchClause}
            ResourceList >> Resource & {";" & Resource}
            ArgumentList >> Expression & {"," & Expression}
            DimExprs >> DimExpr & {DimExpr}

            //Sequence with boolean for the optional token
            ModuleDeclaration >> {Annotation} & ["open"] & "module" & Identifier & {"." & Identifier}
                    & "{" & {ModuleDirective} & "}"
            EnumBody >> "{" & [EnumConstantList] & [","] & [EnumBodyDeclarations] & "}"
            ResourceSpecification >> "(" & ResourceList & [";"] & ")"
            ElementValueArrayInitializer >> "{" & [ElementValueList] & [","] & "}"
            ArrayInitializer >> "{" & [VariableInitializerList] & [","] & "}"

            //After token removal, {{ x }} should be considered as { x } (x is one element)
            ClassOrInterfaceTypeToInstantiate >> {Annotation} & Identifier & {"." & {Annotation}
                    & Identifier} & [TypeArgumentsOrDiamond]
            Dims >> { Annotation } & "[" & "]" & { { Annotation } & "[" & "]" }

            //After token removal, [[ x ]] should be considered as [ x ] (x is one element)
            EnumConstant >> {EnumConstantModifier} & Identifier & ["(" & [ArgumentList] & ")"] & [ClassBody]

            //A -> seq1 | seq2 - A is an interface. seq1 and seq2 become classes (named something like A1, A2) that
            //implement A
            PrimitiveType >> { Annotation } & NumericType | { Annotation } & "boolean"
            ClassType >> { Annotation } & TypeIdentifier & [TypeArguments]
                    | PackageName & "." & { Annotation } & TypeIdentifier & [TypeArguments]
                    | ClassOrInterfaceType & "." & { Annotation } & TypeIdentifier & [TypeArguments]
            TypeBound >> "extends" & TypeVariable | "extends" & ClassOrInterfaceType & { AdditionalBound }
            ModuleName >> Identifier
                    | ModuleName & "." & Identifier
            PackageName >> Identifier
                    | PackageName & "." & Identifier
            TypeName >> TypeIdentifier
                    | PackageOrTypeName & "." & TypeIdentifier
            ExpressionName >> Identifier
                    | AmbiguousName & "." & Identifier
            PackageOrTypeName >> Identifier
                    | PackageOrTypeName & "." & Identifier
            AmbiguousName >> Identifier
                    | AmbiguousName & "." & Identifier
            UnannClassType >> TypeIdentifier & [TypeArguments]
                    | PackageName & "." & {Annotation} & TypeIdentifier & [TypeArguments]
                    | UnannClassOrInterfaceType & "." & {Annotation} & TypeIdentifier & [TypeArguments]
            AssertStatement >> "assert" & Expression & ";"
                    | "assert" & Expression & ":" & Expression & ";"
            SwitchBlock >> "{" & SwitchRule & {SwitchRule} & "}"
                    | "{" & {SwitchBlockStatementGroup} & {SwitchLabel & ":"} & "}"
            SwitchRule >> SwitchLabel & "->" & Expression & ";"
                    | SwitchLabel & "->" & Block
                    | SwitchLabel & "->" & ThrowStatement
            SwitchLabel >> "case" & CaseConstant & {"," & CaseConstant}
                    | "default"
            TryStatement >> "try" & Block & Catches
                    | "try" & Block & [Catches] & Finally
                    | TryWithResourcesStatement
            Resource >> {VariableModifier} & LocalVariableType & Identifier & "=" & Expression
                    | VariableAccess
            MethodHeader >> Result & MethodDeclarator & [Throws]
                    | TypeParameters & {Annotation} & Result & MethodDeclarator & [Throws]
            FormalParameter >> {VariableModifier} & UnannType & VariableDeclaratorId
                    | VariableArityParameter
            ExplicitConstructorInvocation >> [TypeArguments] & "this" & "(" & [ArgumentList] & ")" & ";"
                    | [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"
                    | ExpressionName & "." & [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"
                    | Primary & "." & [TypeArguments] & "super" & "(" & [ArgumentList] & ")" & ";"
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
            LambdaParameters >> "(" & [LambdaParameterList] & ")"
                    | Identifier
            LambdaParameterList >> LambdaParameter & {"," & LambdaParameter}
                    | Identifier & {"," & Identifier}
            LambdaParameter >> {VariableModifier} & LambdaParameterType & VariableDeclaratorId
                    | VariableArityParameter
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
            CastExpression >> "(" & PrimitiveType & ")" & UnaryExpression
                    | "(" & ReferenceType & {AdditionalBound} & ")" & UnaryExpressionNotPlusMinus
                    | "(" & ReferenceType & {AdditionalBound} & ")" & LambdaExpression

            //Left becomes an interface. Each right or option implements it. If right is a sequence, it needs an
            //intermediate class to contain it and implement left
            ClassInstanceCreationExpression >> UnqualifiedClassInstanceCreationExpression
                    | ExpressionName & "." & UnqualifiedClassInstanceCreationExpression
                    | Primary & "." & UnqualifiedClassInstanceCreationExpression

            //Without optimization, this is seq | seq like above. With optimization: A -> B & C | D & C can
            //be handled this way: B and D are classes with a common interface A1, and A is a class containing
            //A1 and C
            ArrayType >> PrimitiveType & Dims
                    | ClassOrInterfaceType & Dims
                    | TypeVariable & Dims
            UnannArrayType >> UnannPrimitiveType & Dims
                    | UnannClassOrInterfaceType & Dims
                    | UnannTypeVariable & Dims

            //Optional of more than one element, or list of more than one element, require an intermediate type...
            ModuleDirective >> "requires" & {RequiresModifier} & ModuleName & ";"
                    | "exports" & PackageName & ["to" & ModuleName & {"," & ModuleName}] & ";"
                    | "opens" & PackageName & ["to" & ModuleName & {"," & ModuleName}] & ";"
                    | "uses" & TypeName & ";"
                    | "provides" & TypeName & "with" & TypeName & {"," & TypeName} & ";"

            //TODO: A list of non informative - should we replace with an integer?
            ClassLiteral >> TypeName & {"[" & "]"} & "." & "class"
                    | NumericType & {"[" & "]"} & "." & "class"
                    | "boolean" & {"[" & "]"} & "." & "class"
                    | "void" & "." & "class"

            //TODO: interesting case. If token removal makes options equal, the token / token combination
            //should become an enum!
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
            WildcardBounds >> "extends" & ReferenceType
                    | "super" & ReferenceType

        }

        println(java.follow())
    }
}
