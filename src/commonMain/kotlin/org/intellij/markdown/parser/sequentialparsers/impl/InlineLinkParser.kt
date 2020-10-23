package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LocalParsingResult
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class InlineLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val inlineLink = parseInlineLink(iterator)
                if (inlineLink != null) {
                    iterator = inlineLink.iteratorPosition.advance()
                    result = result.withOtherParsingResult(inlineLink)
                    continue
                }
            }

            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    companion object {
        fun parseInlineLink(iterator: TokensCache.Iterator): LocalParsingResult? {
            val startIndex = iterator.index
            var it = iterator

            val linkText = LinkParserUtil.parseLinkText(it)
                    ?: return null
            it = linkText.iteratorPosition
            if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
                return null
            }

            it = it.advance().advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
            val linkDestination = LinkParserUtil.parseLinkDestination(it)
            if (linkDestination != null) {
                it = linkDestination.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            val linkTitle = LinkParserUtil.parseLinkTitle(it)
            if (linkTitle != null) {
                it = linkTitle.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            if (it.type != MarkdownTokenTypes.RPAREN) {
                return null
            }

            return LocalParsingResult(it,
                    linkText.parsedNodes
                            + (linkDestination?.parsedNodes ?: emptyList())
                            + (linkTitle?.parsedNodes ?: emptyList())
                            + SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.INLINE_LINK),
                    linkText.rangesToProcessFurther)
        }
    }
}
