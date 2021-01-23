#ifndef ASTYLE_H
#define ASTYLE_H

#ifdef __VMS
#define __USE_STD_IOSTREAM 1
#include <assert>
#else
#include <cassert>
#endif

#include <cctype>
#include <iostream>
#include <memory>
#include <string>
#include <vector>

#ifdef __GNUC__
#include <cstring>
#endif

#ifdef _MSC_VER
#pragma warning(disable: 4267)
#endif

#ifdef __BORLANDC__
#pragma warn -8004
#endif

#ifdef __GNUC__
#pragma GCC diagnostic ignored "-Wconversion"
#endif

#ifdef __INTEL_COMPILER

#endif

#ifdef __clang__
#pragma clang diagnostic ignored "-Wshorten-64-to-32"
#endif

namespace astyle {

    using namespace std;

    enum FileType { C_TYPE = 0, JAVA_TYPE = 1, SHARP_TYPE = 2 };

    enum FormatStyle {
        STYLE_NONE,
        STYLE_ALLMAN,
        STYLE_JAVA,
        STYLE_KR,
        STYLE_STROUSTRUP,
        STYLE_WHITESMITH,
        STYLE_VTK,
        STYLE_RATLIFF,
        STYLE_GNU,
        STYLE_LINUX,
        STYLE_HORSTMANN,
        STYLE_1TBS,
        STYLE_GOOGLE,
        STYLE_MOZILLA,
        STYLE_WEBKIT,
        STYLE_PICO,
        STYLE_LISP
    };

    enum BraceMode {
        NONE_MODE,
        ATTACH_MODE,
        BREAK_MODE,
        LINUX_MODE,
        RUN_IN_MODE
    };

    enum BraceType : size_t {
        NULL_TYPE        = 0,
        NAMESPACE_TYPE   = 1,
        CLASS_TYPE       = 2,
        STRUCT_TYPE      = 4,
        INTERFACE_TYPE   = 8,
        DEFINITION_TYPE  = 16,
        COMMAND_TYPE     = 32,
        ARRAY_NIS_TYPE   = 64,
        ENUM_TYPE        = 128,
        INIT_TYPE        = 256,
        ARRAY_TYPE       = 512,
        EXTERN_TYPE      = 1024,
        EMPTY_BLOCK_TYPE = 2048,
        BREAK_BLOCK_TYPE = 4096,
        SINGLE_LINE_TYPE = 8192
    };

    enum MinConditional {
        MINCOND_ZERO,
        MINCOND_ONE,
        MINCOND_TWO,
        MINCOND_ONEHALF,
        MINCOND_END
    };

    enum ObjCColonPad {
        COLON_PAD_NO_CHANGE,
        COLON_PAD_NONE,
        COLON_PAD_ALL,
        COLON_PAD_AFTER,
        COLON_PAD_BEFORE
    };

    enum PointerAlign {
        PTR_ALIGN_NONE,
        PTR_ALIGN_TYPE,
        PTR_ALIGN_MIDDLE,
        PTR_ALIGN_NAME
    };

    enum ReferenceAlign {
        REF_ALIGN_NONE   = PTR_ALIGN_NONE,
        REF_ALIGN_TYPE   = PTR_ALIGN_TYPE,
        REF_ALIGN_MIDDLE = PTR_ALIGN_MIDDLE,
        REF_ALIGN_NAME   = PTR_ALIGN_NAME,
        REF_SAME_AS_PTR
    };

    enum FileEncoding {
        ENCODING_8BIT,
        UTF_8BOM,
        UTF_16BE,
        UTF_16LE,
        UTF_32BE,
        UTF_32LE
    };

    enum LineEndFormat {
        LINEEND_DEFAULT,
        LINEEND_WINDOWS,
        LINEEND_LINUX,
        LINEEND_MACOLD,
        LINEEND_CRLF = LINEEND_WINDOWS,
        LINEEND_LF   = LINEEND_LINUX,
        LINEEND_CR   = LINEEND_MACOLD
    };

    class ASSourceIterator {
    public:
        ASSourceIterator() = default;
        virtual ~ASSourceIterator() = default;
        virtual streamoff getPeekStart() const = 0;
        virtual int getStreamLength() const = 0;
        virtual bool hasMoreLines() const = 0;
        virtual string nextLine(bool emptyLineWasDeleted) = 0;
        virtual string peekNextLine() = 0;
        virtual void peekReset() = 0;
        virtual streamoff tellg() = 0;
    };

    class ASPeekStream {
    private:
        ASSourceIterator *sourceIterator;
        bool needReset;

    public:
        explicit ASPeekStream(ASSourceIterator *sourceIterator_) {
            sourceIterator = sourceIterator_;
            needReset = false;
        }

        ~ASPeekStream() {
            if (needReset) sourceIterator->peekReset();
        }

        bool hasMoreLines() const {
            return sourceIterator->hasMoreLines();
        }

        string peekNextLine() {
            needReset = true;
            return sourceIterator->peekNextLine();
        }
    };

    class ASResource {
    public:
        void buildAssignmentOperators(vector<const string *> *assignmentOperators);
        void buildCastOperators(vector<const string *> *castOperators);
        void buildHeaders(vector<const string *> *headers, int fileType, bool beautifier = false);
        void buildIndentableMacros(vector<const pair<const string, const string>* > *indentableMacros);
        void buildIndentableHeaders(vector<const string *> *indentableHeaders);
        void buildNonAssignmentOperators(vector<const string *> *nonAssignmentOperators);
        void buildNonParenHeaders(vector<const string *> *nonParenHeaders, int fileType, bool beautifier = false);
        void buildOperators(vector<const string *> *operators, int fileType);
        void buildPreBlockStatements(vector<const string *> *preBlockStatements, int fileType);
        void buildPreCommandHeaders(vector<const string *> *preCommandHeaders, int fileType);
        void buildPreDefinitionHeaders(vector<const string *> *preDefinitionHeaders, int fileType);

    public:
        static const string AS_IF, AS_ELSE;
        static const string AS_DO, AS_WHILE;
        static const string AS_FOR;
        static const string AS_SWITCH, AS_CASE, AS_DEFAULT;
        static const string AS_TRY, AS_CATCH, AS_THROW, AS_THROWS, AS_FINALLY, AS_USING;
        static const string _AS_TRY, _AS_FINALLY, _AS_EXCEPT;
        static const string AS_PUBLIC, AS_PROTECTED, AS_PRIVATE;
        static const string AS_CLASS, AS_STRUCT, AS_UNION, AS_INTERFACE, AS_NAMESPACE;
        static const string AS_MODULE;
        static const string AS_END;
        static const string AS_SELECTOR;
        static const string AS_EXTERN, AS_ENUM;
        static const string AS_FINAL, AS_OVERRIDE;
        static const string AS_STATIC, AS_CONST, AS_SEALED, AS_VOLATILE, AS_NEW, AS_DELETE;
        static const string AS_NOEXCEPT, AS_INTERRUPT, AS_AUTORELEASEPOOL;
        static const string AS_WHERE, AS_LET, AS_SYNCHRONIZED;
        static const string AS_OPERATOR, AS_TEMPLATE;
        static const string AS_OPEN_BRACE, AS_CLOSE_BRACE;
        static const string AS_OPEN_LINE_COMMENT, AS_OPEN_COMMENT, AS_CLOSE_COMMENT;
        static const string AS_BAR_DEFINE, AS_BAR_INCLUDE, AS_BAR_IF, AS_BAR_EL, AS_BAR_ENDIF;
        static const string AS_AUTO, AS_RETURN;
        static const string AS_CIN, AS_COUT, AS_CERR;
        static const string AS_ASSIGN, AS_PLUS_ASSIGN, AS_MINUS_ASSIGN, AS_MULT_ASSIGN;
        static const string AS_DIV_ASSIGN, AS_MOD_ASSIGN, AS_XOR_ASSIGN, AS_OR_ASSIGN, AS_AND_ASSIGN;
        static const string AS_GR_GR_ASSIGN, AS_LS_LS_ASSIGN, AS_GR_GR_GR_ASSIGN, AS_LS_LS_LS_ASSIGN;
        static const string AS_GCC_MIN_ASSIGN, AS_GCC_MAX_ASSIGN;
        static const string AS_EQUAL, AS_PLUS_PLUS, AS_MINUS_MINUS, AS_NOT_EQUAL, AS_GR_EQUAL;
        static const string AS_LS_EQUAL, AS_LS_LS_LS, AS_LS_LS, AS_GR_GR_GR, AS_GR_GR;
        static const string AS_QUESTION_QUESTION, AS_LAMBDA;
        static const string AS_ARROW, AS_AND, AS_OR;
        static const string AS_SCOPE_RESOLUTION;
        static const string AS_PLUS, AS_MINUS, AS_MULT, AS_DIV, AS_MOD, AS_GR, AS_LS;
        static const string AS_NOT, AS_BIT_XOR, AS_BIT_OR, AS_BIT_AND, AS_BIT_NOT;
        static const string AS_QUESTION, AS_COLON, AS_SEMICOLON, AS_COMMA;
        static const string AS_ASM, AS__ASM__, AS_MS_ASM, AS_MS__ASM;
        static const string AS_QFOREACH, AS_QFOREVER, AS_FOREVER;
        static const string AS_FOREACH, AS_LOCK, AS_UNSAFE, AS_FIXED;
        static const string AS_GET, AS_SET, AS_ADD, AS_REMOVE;
        static const string AS_DELEGATE, AS_UNCHECKED;
        static const string AS_CONST_CAST, AS_DYNAMIC_CAST, AS_REINTERPRET_CAST, AS_STATIC_CAST;
        static const string AS_NS_DURING, AS_NS_HANDLER;
    };

    class ASBase : protected ASResource {
    private:

        int baseFileType = C_TYPE;

    protected:
        ASBase() = default;

    protected:
        void init(int fileTypeArg) {
            baseFileType = fileTypeArg;
        }
        bool isCStyle() const {
            return (baseFileType == C_TYPE);
        }
        bool isJavaStyle() const {
            return (baseFileType == JAVA_TYPE);
        }
        bool isSharpStyle() const {
            return (baseFileType == SHARP_TYPE);
        }
        bool isWhiteSpace(char ch) const {
            return (ch == ' ' || ch == '\t');
        }

    protected:
        const string *findHeader(const string &line, int i,
                                 const vector<const string *> *possibleHeaders) const;
        bool findKeyword(const string &line, int i, const string &keyword) const;
        const string *findOperator(const string &line, int i,
                                   const vector<const string *> *possibleOperators) const;
        string getCurrentWord(const string &line, size_t index) const;
        bool isDigit(char ch) const;
        bool isLegalNameChar(char ch) const;
        bool isCharPotentialHeader(const string &line, size_t i) const;
        bool isCharPotentialOperator(char ch) const;
        bool isDigitSeparator(const string &line, int i) const;
        char peekNextChar(const string &line, int i) const;

    };

    class ASBeautifier : protected ASBase {
    public:
        ASBeautifier();
        virtual ~ASBeautifier();
        ASBeautifier(const ASBeautifier &other);
        ASBeautifier &operator=(ASBeautifier const &) = delete;
        ASBeautifier(ASBeautifier &&)                 = delete;
        ASBeautifier &operator=(ASBeautifier &&)      = delete;
        virtual void init(ASSourceIterator *iter);

        virtual string beautify(const string &originalLine);
        void setCaseIndent(bool state);
        void setClassIndent(bool state);
        void setContinuationIndentation(int indent = 1);
        void setCStyle();
        void setDefaultTabLength();
        void setEmptyLineFill(bool state);
        void setForceTabXIndentation(int length);
        void setAfterParenIndent(bool state);
        void setJavaStyle();
        void setLabelIndent(bool state);
        void setMaxContinuationIndentLength(int max);
        void setMaxInStatementIndentLength(int max);
        void setMinConditionalIndentOption(int min);
        void setMinConditionalIndentLength();
        void setModeManuallySet(bool state);
        void setModifierIndent(bool state);
        void setNamespaceIndent(bool state);
        void setAlignMethodColon(bool state);
        void setSharpStyle();
        void setSpaceIndentation(int length = 4);
        void setSwitchIndent(bool state);
        void setTabIndentation(int length = 4, bool forceTabs = false);
        void setPreprocDefineIndent(bool state);
        void setPreprocConditionalIndent(bool state);
        int  getBeautifierFileType() const;
        int  getFileType() const;
        int  getIndentLength() const;
        int  getTabLength() const;
        string getIndentString() const;
        string getNextWord(const string &line, size_t currPos) const;
        bool getAlignMethodColon() const;
        bool getBraceIndent() const;
        bool getBlockIndent() const;
        bool getCaseIndent() const;
        bool getClassIndent() const;
        bool getEmptyLineFill() const;
        bool getForceTabIndentation() const;
        bool getModeManuallySet() const;
        bool getModifierIndent() const;
        bool getNamespaceIndent() const;
        bool getPreprocDefineIndent() const;
        bool getSwitchIndent() const;

    protected:
        void deleteBeautifierVectors();
        int  getNextProgramCharDistance(const string &line, int i) const;
        int  indexOf(const vector<const string *> &container, const string *element) const;
        void setBlockIndent(bool state);
        void setBraceIndent(bool state);
        void setBraceIndentVtk(bool state);
        string extractPreprocessorStatement(const string &line) const;
        string trim(const string &str) const;
        string rtrim(const string &str) const;

        int  inLineNumber;
        int  runInIndentContinuation;
        int  nonInStatementBrace;
        int  objCColonAlignSubsequent;
        bool lineCommentNoBeautify;
        bool isElseHeaderIndent;
        bool isCaseHeaderCommentIndent;
        bool isNonInStatementArray;
        bool isSharpAccessor;
        bool isSharpDelegate;
        bool isInExternC;
        bool isInBeautifySQL;
        bool isInIndentableStruct;
        bool isInIndentablePreproc;

    private:
        void adjustObjCMethodDefinitionIndentation(const string &line_);
        void adjustObjCMethodCallIndentation(const string &line_);
        void adjustParsedLineIndentation(size_t iPrelim, bool isInExtraHeaderIndent);
        void computePreliminaryIndentation();
        void parseCurrentLine(const string &line);
        void popLastContinuationIndent();
        void processPreprocessor(const string &preproc, const string &line);
        void registerContinuationIndent(const string &line, int i, int spaceIndentCount_,
                                        int tabIncrementIn, int minIndent, bool updateParenStack);
        void registerContinuationIndentColon(const string &line, int i, int tabIncrementIn);
        void initVectors();
        void initTempStacksContainer(vector<vector<const string *>*> *&container,
                                     vector<vector<const string *>*> *value);
        void clearObjCMethodDefinitionAlignment();
        void deleteBeautifierContainer(vector<ASBeautifier *> *&container);
        void deleteTempStacksContainer(vector<vector<const string *>*> *&container);
        int  adjustIndentCountForBreakElseIfComments() const;
        int  computeObjCColonAlignment(const string &line, int colonAlignPosition) const;
        int  convertTabToSpaces(int i, int tabIncrementIn) const;
        int  findObjCColonAlignment(const string &line) const;
        int  getContinuationIndentAssign(const string &line, size_t currPos) const;
        int  getContinuationIndentComma(const string &line, size_t currPos) const;
        int  getObjCFollowingKeyword(const string &line, int bracePos) const;
        bool isIndentedPreprocessor(const string &line, size_t currPos) const;
        bool isLineEndComment(const string &line, int startPos) const;
        bool isPreprocessorConditionalCplusplus(const string &line) const;
        bool isInPreprocessorUnterminatedComment(const string &line);
        bool isTopLevel() const;
        bool statementEndsWithComma(const string &line, int index) const;
        const string &getIndentedLineReturn(const string &newLine, const string &originalLine) const;
        string getIndentedSpaceEquivalent(const string &line_) const;
        string preLineWS(int lineIndentCount, int lineSpaceIndentCount) const;
        template<typename T> void deleteContainer(T &container);
        template<typename T> void initContainer(T &container, T value);
        vector<vector<const string *>*> *copyTempStacks(const ASBeautifier &other) const;
        pair<int, int> computePreprocessorIndent();

    private:
        int beautifierFileType;
        vector<const string *> *headers;
        vector<const string *> *nonParenHeaders;
        vector<const string *> *preBlockStatements;
        vector<const string *> *preCommandHeaders;
        vector<const string *> *assignmentOperators;
        vector<const string *> *nonAssignmentOperators;
        vector<const string *> *indentableHeaders;

        vector<ASBeautifier *> *waitingBeautifierStack;
        vector<ASBeautifier *> *activeBeautifierStack;
        vector<size_t> *waitingBeautifierStackLengthStack;
        vector<size_t> *activeBeautifierStackLengthStack;
        vector<const string *> *headerStack;
        vector<vector<const string *>* > *tempStacks;
        vector<int> *parenDepthStack;
        vector<bool> *blockStatementStack;
        vector<bool> *parenStatementStack;
        vector<bool> *braceBlockStateStack;
        vector<int> *continuationIndentStack;
        vector<size_t> *continuationIndentStackSizeStack;
        vector<int> *parenIndentStack;
        vector<pair<int, int> > *preprocIndentStack;

        ASSourceIterator *sourceIterator;
        const string *currentHeader;
        const string *previousLastLineHeader;
        const string *probationHeader;
        const string *lastLineHeader;
        string indentString;
        string verbatimDelimiter;
        bool isInQuote;
        bool isInVerbatimQuote;
        bool haveLineContinuationChar;
        bool isInAsm;
        bool isInAsmOneLine;
        bool isInAsmBlock;
        bool isInComment;
        bool isInPreprocessorComment;
        bool isInRunInComment;
        bool isInCase;
        bool isInQuestion;
        bool isContinuation;
        bool isInHeader;
        bool isInTemplate;
        bool isInDefine;
        bool isInDefineDefinition;
        bool classIndent;
        bool isIndentModeOff;
        bool isInClassHeader;
        bool isInClassHeaderTab;
        bool isInClassInitializer;
        bool isInClass;
        bool isInObjCMethodDefinition;
        bool isInObjCMethodCall;
        bool isInObjCMethodCallFirst;
        bool isImmediatelyPostObjCMethodDefinition;
        bool isImmediatelyPostObjCMethodCall;
        bool isInIndentablePreprocBlock;
        bool isInObjCInterface;
        bool isInEnum;
        bool isInEnumTypeID;
        bool isInLet;
        bool isInTrailingReturnType;
        bool modifierIndent;
        bool switchIndent;
        bool caseIndent;
        bool namespaceIndent;
        bool blockIndent;
        bool braceIndent;
        bool braceIndentVtk;
        bool shouldIndentAfterParen;
        bool labelIndent;
        bool shouldIndentPreprocDefine;
        bool isInConditional;
        bool isModeManuallySet;
        bool shouldForceTabIndentation;
        bool emptyLineFill;
        bool backslashEndsPrevLine;
        bool lineOpensWithLineComment;
        bool lineOpensWithComment;
        bool lineStartsInComment;
        bool blockCommentNoIndent;
        bool blockCommentNoBeautify;
        bool previousLineProbationTab;
        bool lineBeginsWithOpenBrace;
        bool lineBeginsWithCloseBrace;
        bool lineBeginsWithComma;
        bool lineIsCommentOnly;
        bool lineIsLineCommentOnly;
        bool shouldIndentBracedLine;
        bool isInSwitch;
        bool foundPreCommandHeader;
        bool foundPreCommandMacro;
        bool shouldAlignMethodColon;
        bool shouldIndentPreprocConditional;
        int  indentCount;
        int  spaceIndentCount;
        int  spaceIndentObjCMethodAlignment;
        int  bracePosObjCMethodAlignment;
        int  colonIndentObjCMethodAlignment;
        int  lineOpeningBlocksNum;
        int  lineClosingBlocksNum;
        int  fileType;
        int  minConditionalOption;
        int  minConditionalIndent;
        int  parenDepth;
        int  indentLength;
        int  tabLength;
        int  continuationIndent;
        int  blockTabCount;
        int  maxContinuationIndent;
        int  classInitializerIndents;
        int  templateDepth;
        int  squareBracketCount;
        int  prevFinalLineSpaceIndentCount;
        int  prevFinalLineIndentCount;
        int  defineIndentCount;
        int  preprocBlockIndent;
        char quoteChar;
        char prevNonSpaceCh;
        char currentNonSpaceCh;
        char currentNonLegalCh;
        char prevNonLegalCh;
    };

    class ASEnhancer : protected ASBase {
    public:
        ASEnhancer() = default;
        void init(int, int, int, bool, bool, bool, bool, bool, bool, bool,
                  vector<const pair<const string, const string>* > *);
        void enhance(string &line, bool isInNamespace, bool isInPreprocessor, bool isInSQL);

    private:
        void   convertForceTabIndentToSpaces(string  &line) const;
        void   convertSpaceIndentToForceTab(string &line) const;
        size_t findCaseColon(const string  &line, size_t caseIndex) const;
        int    indentLine(string  &line, int indent) const;
        bool   isBeginDeclareSectionSQL(const string  &line, size_t index) const;
        bool   isEndDeclareSectionSQL(const string  &line, size_t index) const;
        bool   isOneLineBlockReached(const string &line, int startChar) const;
        void   parseCurrentLine(string &line, bool isInPreprocessor, bool isInSQL);
        size_t processSwitchBlock(string  &line, size_t index);
        int    unindentLine(string  &line, int unindent) const;

    private:

        int  indentLength;
        int  tabLength;
        bool useTabs;
        bool forceTab;
        bool namespaceIndent;
        bool caseIndent;
        bool preprocBlockIndent;
        bool preprocDefineIndent;
        bool emptyLineFill;

        int  lineNumber;
        bool isInQuote;
        bool isInComment;
        char quoteChar;

        int  braceCount;
        int  switchDepth;
        int  eventPreprocDepth;
        bool lookingForCaseBrace;
        bool unindentNextLine;
        bool shouldUnindentLine;
        bool shouldUnindentComment;

        struct SwitchVariables {
            int  switchBraceCount;
            int  unindentDepth;
            bool unindentCase;
        };

        SwitchVariables sw;
        vector<SwitchVariables> switchStack;

        bool nextLineIsEventIndent;
        bool isInEventTable;
        vector<const pair<const string, const string>* > *indentableMacros;

        bool nextLineIsDeclareIndent;
        bool isInDeclareSection;

    };

    class ASFormatter : public ASBeautifier {
    public:
        ASFormatter();
        ~ASFormatter() override;
        ASFormatter(const ASFormatter &)            = delete;
        ASFormatter &operator=(ASFormatter const &) = delete;
        ASFormatter(ASFormatter &&)                 = delete;
        ASFormatter &operator=(ASFormatter &&)      = delete;
        void init(ASSourceIterator *si) override;

        bool hasMoreLines() const;
        string nextLine();
        LineEndFormat getLineEndFormat() const;
        bool getIsLineReady() const;
        void setFormattingStyle(FormatStyle style);
        void setAddBracesMode(bool state);
        void setAddOneLineBracesMode(bool state);
        void setRemoveBracesMode(bool state);
        void setAttachClass(bool state);
        void setAttachClosingWhile(bool state);
        void setAttachExternC(bool state);
        void setAttachNamespace(bool state);
        void setAttachInline(bool state);
        void setBraceFormatMode(BraceMode mode);
        void setBreakAfterMode(bool state);
        void setBreakClosingHeaderBracesMode(bool state);
        void setBreakBlocksMode(bool state);
        void setBreakClosingHeaderBlocksMode(bool state);
        void setBreakElseIfsMode(bool state);
        void setBreakOneLineBlocksMode(bool state);
        void setBreakOneLineHeadersMode(bool state);
        void setBreakOneLineStatementsMode(bool state);
        void setMethodPrefixPaddingMode(bool state);
        void setMethodPrefixUnPaddingMode(bool state);
        void setReturnTypePaddingMode(bool state);
        void setReturnTypeUnPaddingMode(bool state);
        void setParamTypePaddingMode(bool state);
        void setParamTypeUnPaddingMode(bool state);
        void setCloseTemplatesMode(bool state);
        void setCommaPaddingMode(bool state);
        void setDeleteEmptyLinesMode(bool state);
        void setBreakReturnType(bool state);
        void setBreakReturnTypeDecl(bool state);
        void setAttachReturnType(bool state);
        void setAttachReturnTypeDecl(bool state);
        void setIndentCol1CommentsMode(bool state);
        void setLineEndFormat(LineEndFormat fmt);
        void setMaxCodeLength(int max);
        void setObjCColonPaddingMode(ObjCColonPad mode);
        void setOperatorPaddingMode(bool state);
        void setParensOutsidePaddingMode(bool state);
        void setParensFirstPaddingMode(bool state);
        void setParensInsidePaddingMode(bool state);
        void setParensHeaderPaddingMode(bool state);
        void setParensUnPaddingMode(bool state);
        void setPointerAlignment(PointerAlign alignment);
        void setPreprocBlockIndent(bool state);
        void setReferenceAlignment(ReferenceAlign alignment);
        void setStripCommentPrefix(bool state);
        void setTabSpaceConversionMode(bool state);
        size_t getChecksumIn() const;
        size_t getChecksumOut() const;
        int  getChecksumDiff() const;
        int  getFormatterFileType() const;

        void setAddBracketsMode(bool state);
        void setAddOneLineBracketsMode(bool state);
        void setRemoveBracketsMode(bool state);
        void setBreakClosingHeaderBracketsMode(bool state);

    private:
        template<typename T> void deleteContainer(T &container);
        template<typename T> void initContainer(T &container, T value);
        char peekNextChar() const;
        BraceType getBraceType();
        bool adjustChecksumIn(int adjustment);
        bool computeChecksumIn(const string &currentLine_);
        bool computeChecksumOut(const string &beautifiedLine);
        bool addBracesToStatement();
        bool removeBracesFromStatement();
        bool commentAndHeaderFollows();
        bool getNextChar();
        bool getNextLine(bool emptyLineWasDeleted = false);
        bool isArrayOperator() const;
        bool isBeforeComment() const;
        bool isBeforeAnyComment() const;
        bool isBeforeAnyLineEndComment(int startPos) const;
        bool isBeforeMultipleLineEndComments(int startPos) const;
        bool isBraceType(BraceType a, BraceType b) const;
        bool isClassInitializer() const;
        bool isClosingHeader(const string *header) const;
        bool isCurrentBraceBroken() const;
        bool isDereferenceOrAddressOf() const;
        bool isExecSQL(const string &line, size_t index) const;
        bool isEmptyLine(const string &line) const;
        bool isExternC() const;
        bool isMultiStatementLine() const;
        bool isNextWordSharpNonParenHeader(int startChar) const;
        bool isNonInStatementArrayBrace() const;
        bool isNumericVariable(const string &word) const;
        bool isOkToSplitFormattedLine();
        bool isPointerOrReference() const;
        bool isPointerOrReferenceCentered() const;
        bool isPointerOrReferenceVariable(const string &word) const;
        bool isPointerToPointer(const string &line, int currPos) const;
        bool isSharpStyleWithParen(const string *header) const;
        bool isStructAccessModified(const string &firstLine, size_t index) const;
        bool isIndentablePreprocessorBlock(const string &firstLine, size_t index);
        bool isNDefPreprocStatement(const string &nextLine_, const string &preproc) const;
        bool isUnaryOperator() const;
        bool isUniformInitializerBrace() const;
        bool isImmediatelyPostCast() const;
        bool isInExponent() const;
        bool isInSwitchStatement() const;
        bool isNextCharOpeningBrace(int startChar) const;
        bool isOkToBreakBlock(BraceType braceType) const;
        bool isOperatorPaddingDisabled() const;
        bool pointerSymbolFollows() const;
        int  findObjCColonAlignment() const;
        int  getCurrentLineCommentAdjustment();
        int  getNextLineCommentAdjustment();
        int  isOneLineBlockReached(const string &line, int startChar) const;
        void adjustComments();
        void appendChar(char ch, bool canBreakLine);
        void appendCharInsideComments();
        void appendClosingHeader();
        void appendOperator(const string &sequence, bool canBreakLine = true);
        void appendSequence(const string &sequence, bool canBreakLine = true);
        void appendSpacePad();
        void appendSpaceAfter();
        void breakLine(bool isSplitLine = false);
        void buildLanguageVectors();
        void updateFormattedLineSplitPoints(char appendedChar);
        void updateFormattedLineSplitPointsOperator(const string &sequence);
        void checkIfTemplateOpener();
        void clearFormattedLineSplitPoints();
        void convertTabToSpaces();
        void deleteContainer(vector<BraceType> *&container);
        void findReturnTypeSplitPoint(const string &firstLine);
        void formatArrayRunIn();
        void formatRunIn();
        void formatArrayBraces(BraceType braceType, bool isOpeningArrayBrace);
        void formatClosingBrace(BraceType braceType);
        void formatCommentBody();
        void formatCommentOpener();
        void formatCommentCloser();
        void formatLineCommentBody();
        void formatLineCommentOpener();
        void formatOpeningBrace(BraceType braceType);
        void formatQuoteBody();
        void formatQuoteOpener();
        void formatPointerOrReference();
        void formatPointerOrReferenceCast();
        void formatPointerOrReferenceToMiddle();
        void formatPointerOrReferenceToName();
        void formatPointerOrReferenceToType();
        void fixOptionVariableConflicts();
        void goForward(int i);
        void isLineBreakBeforeClosingHeader();
        void initContainer(vector<BraceType> *&container, vector<BraceType> *value);
        void initNewLine();
        void padObjCMethodColon();
        void padObjCMethodPrefix();
        void padObjCParamType();
        void padObjCReturnType();
        void padOperators(const string *newOperator);
        void padParens();
        void processPreprocessor();
        void resetEndOfStatement();
        void setAttachClosingBraceMode(bool state);
        void stripCommentPrefix();
        void testForTimeToSplitFormattedLine();
        void trimContinuationLine();
        void updateFormattedLineSplitPointsPointerOrReference(size_t index);
        size_t findFormattedLineSplitPoint() const;
        size_t findNextChar(const string &line, char searchChar, int searchStart = 0) const;
        const string *checkForHeaderFollowingComment(const string &firstLine) const;
        const string *getFollowingOperator() const;
        string getPreviousWord(const string &line, int currPos) const;
        string peekNextText(const string &firstLine,
                            bool endOnEmptyLine = false,
                            const shared_ptr<ASPeekStream> &streamArg = nullptr) const;

    private:
        int formatterFileType;
        vector<const string *> *headers;
        vector<const string *> *nonParenHeaders;
        vector<const string *> *preDefinitionHeaders;
        vector<const string *> *preCommandHeaders;
        vector<const string *> *operators;
        vector<const string *> *assignmentOperators;
        vector<const string *> *castOperators;
        vector<const pair<const string, const string>* > *indentableMacros;

        ASSourceIterator *sourceIterator;
        ASEnhancer *enhancer;

        vector<const string *> *preBraceHeaderStack;
        vector<BraceType> *braceTypeStack;
        vector<int> *parenStack;
        vector<bool> *structStack;
        vector<bool> *questionMarkStack;

        string currentLine;
        string formattedLine;
        string readyFormattedLine;
        string verbatimDelimiter;
        const string *currentHeader;
        char currentChar;
        char previousChar;
        char previousNonWSChar;
        char previousCommandChar;
        char quoteChar;
        streamoff preprocBlockEnd;
        int  charNum;
        int  runInIndentChars;
        int  nextLineSpacePadNum;
        int  objCColonAlign;
        int  preprocBraceTypeStackSize;
        int  spacePadNum;
        int  tabIncrementIn;
        int  templateDepth;
        int  squareBracketCount;
        size_t checksumIn;
        size_t checksumOut;
        size_t currentLineFirstBraceNum;
        size_t formattedLineCommentNum;
        size_t leadingSpaces;
        size_t maxCodeLength;
        size_t methodAttachCharNum;
        size_t methodAttachLineNum;
        size_t methodBreakCharNum;
        size_t methodBreakLineNum;

        size_t maxSemi;
        size_t maxAndOr;
        size_t maxComma;
        size_t maxParen;
        size_t maxWhiteSpace;
        size_t maxSemiPending;
        size_t maxAndOrPending;
        size_t maxCommaPending;
        size_t maxParenPending;
        size_t maxWhiteSpacePending;

        size_t previousReadyFormattedLineLength;
        FormatStyle formattingStyle;
        BraceMode braceFormatMode;
        BraceType previousBraceType;
        PointerAlign pointerAlignment;
        ReferenceAlign referenceAlignment;
        ObjCColonPad objCColonPadMode;
        LineEndFormat lineEnd;
        bool isVirgin;
        bool isInVirginLine;
        bool shouldPadCommas;
        bool shouldPadOperators;
        bool shouldPadParensOutside;
        bool shouldPadFirstParen;
        bool shouldPadParensInside;
        bool shouldPadHeader;
        bool shouldStripCommentPrefix;
        bool shouldUnPadParens;
        bool shouldConvertTabs;
        bool shouldIndentCol1Comments;
        bool shouldIndentPreprocBlock;
        bool shouldCloseTemplates;
        bool shouldAttachExternC;
        bool shouldAttachNamespace;
        bool shouldAttachClass;
        bool shouldAttachClosingWhile;
        bool shouldAttachInline;
        bool isInLineComment;
        bool isInComment;
        bool isInCommentStartLine;
        bool noTrimCommentContinuation;
        bool isInPreprocessor;
        bool isInPreprocessorDefineDef;
        bool isInPreprocessorBeautify;
        bool isInTemplate;
        bool doesLineStartComment;
        bool lineEndsInCommentOnly;
        bool lineIsCommentOnly;
        bool lineIsLineCommentOnly;
        bool lineIsEmpty;
        bool isImmediatelyPostCommentOnly;
        bool isImmediatelyPostEmptyLine;
        bool isInClassInitializer;
        bool isInQuote;
        bool isInVerbatimQuote;
        bool haveLineContinuationChar;
        bool isInQuoteContinuation;
        bool isHeaderInMultiStatementLine;
        bool isSpecialChar;
        bool isNonParenHeader;
        bool foundQuestionMark;
        bool foundPreDefinitionHeader;
        bool foundNamespaceHeader;
        bool foundClassHeader;
        bool foundStructHeader;
        bool foundInterfaceHeader;
        bool foundPreCommandHeader;
        bool foundPreCommandMacro;
        bool foundTrailingReturnType;
        bool foundCastOperator;
        bool isInLineBreak;
        bool endOfAsmReached;
        bool endOfCodeReached;
        bool lineCommentNoIndent;
        bool isFormattingModeOff;
        bool isInEnum;
        bool isInExecSQL;
        bool isInAsm;
        bool isInAsmOneLine;
        bool isInAsmBlock;
        bool isLineReady;
        bool elseHeaderFollowsComments;
        bool caseHeaderFollowsComments;
        bool isPreviousBraceBlockRelated;
        bool isInPotentialCalculation;
        bool isCharImmediatelyPostComment;
        bool isPreviousCharPostComment;
        bool isCharImmediatelyPostLineComment;
        bool isCharImmediatelyPostOpenBlock;
        bool isCharImmediatelyPostCloseBlock;
        bool isCharImmediatelyPostTemplate;
        bool isCharImmediatelyPostReturn;
        bool isCharImmediatelyPostThrow;
        bool isCharImmediatelyPostNewDelete;
        bool isCharImmediatelyPostOperator;
        bool isCharImmediatelyPostPointerOrReference;
        bool isInObjCMethodDefinition;
        bool isInObjCInterface;
        bool isInObjCReturnType;
        bool isInObjCParam;
        bool isInObjCSelector;
        bool breakCurrentOneLineBlock;
        bool shouldRemoveNextClosingBrace;
        bool isInBraceRunIn;
        bool returnTypeChecked;
        bool currentLineBeginsWithBrace;
        bool attachClosingBraceMode;
        bool shouldBreakOneLineBlocks;
        bool shouldBreakOneLineHeaders;
        bool shouldBreakOneLineStatements;
        bool shouldBreakClosingHeaderBraces;
        bool shouldBreakElseIfs;
        bool shouldBreakLineAfterLogical;
        bool shouldAddBraces;
        bool shouldAddOneLineBraces;
        bool shouldRemoveBraces;
        bool shouldPadMethodColon;
        bool shouldPadMethodPrefix;
        bool shouldReparseCurrentChar;
        bool shouldUnPadMethodPrefix;
        bool shouldPadReturnType;
        bool shouldUnPadReturnType;
        bool shouldPadParamType;
        bool shouldUnPadParamType;
        bool shouldDeleteEmptyLines;
        bool shouldBreakReturnType;
        bool shouldBreakReturnTypeDecl;
        bool shouldAttachReturnType;
        bool shouldAttachReturnTypeDecl;
        bool needHeaderOpeningBrace;
        bool shouldBreakLineAtNextChar;
        bool shouldKeepLineUnbroken;
        bool passedSemicolon;
        bool passedColon;
        bool isImmediatelyPostNonInStmt;
        bool isCharImmediatelyPostNonInStmt;
        bool isImmediatelyPostComment;
        bool isImmediatelyPostLineComment;
        bool isImmediatelyPostEmptyBlock;
        bool isImmediatelyPostObjCMethodPrefix;
        bool isImmediatelyPostPreprocessor;
        bool isImmediatelyPostReturn;
        bool isImmediatelyPostThrow;
        bool isImmediatelyPostNewDelete;
        bool isImmediatelyPostOperator;
        bool isImmediatelyPostTemplate;
        bool isImmediatelyPostPointerOrReference;
        bool shouldBreakBlocks;
        bool shouldBreakClosingHeaderBlocks;
        bool isPrependPostBlockEmptyLineRequested;
        bool isAppendPostBlockEmptyLineRequested;
        bool isIndentableProprocessor;
        bool isIndentableProprocessorBlock;
        bool prependEmptyLine;
        bool appendOpeningBrace;
        bool foundClosingHeader;
        bool isInHeader;
        bool isImmediatelyPostHeader;
        bool isInCase;
        bool isFirstPreprocConditional;
        bool processedFirstConditional;
        bool isJavaStaticConstructor;

    private:

        void appendCurrentChar(bool canBreakLine = true) {
            appendChar(currentChar, canBreakLine);
        }

        bool isSequenceReached(const char *sequence) const {
            return currentLine.compare(charNum, strlen(sequence), sequence) == 0;
        }

        const string *findHeader(const vector<const string *> *headers_) {
            return ASBase::findHeader(currentLine, charNum, headers_);
        }

        const string *findOperator(const vector<const string *> *operators_) {
            return ASBase::findOperator(currentLine, charNum, operators_);
        }
    };

    bool sortOnLength(const string *a, const string *b);
    bool sortOnName(const string *a, const string *b);

}

#endif
