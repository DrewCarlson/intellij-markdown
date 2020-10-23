package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class AtxHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                           productionHolder: ProductionHolder,
                           headerRange: IntRange,
                           tailStartPos: Int,
                           endOfLinePos: Int)
        : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun allowsSubBlocks(): Boolean = false

    init {
        val curPos = productionHolder.currentPosition
        productionHolder.addProduction(listOf(SequentialParser.Node(
                curPos + headerRange.start..curPos + headerRange.endInclusive + 1, MarkdownTokenTypes.ATX_HEADER
        ), SequentialParser.Node(
                curPos + headerRange.endInclusive + 1..tailStartPos, MarkdownTokenTypes.ATX_CONTENT
        ), SequentialParser.Node(
                tailStartPos..endOfLinePos, MarkdownTokenTypes.ATX_HEADER
        )))
    }

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    private val nodeType = calcNodeType(headerRange.endInclusive - headerRange.start + 1)

    private fun calcNodeType(headerSize: Int): IElementType {
        when (headerSize) {
            1 -> return MarkdownElementTypes.ATX_1
            2 -> return MarkdownElementTypes.ATX_2
            3 -> return MarkdownElementTypes.ATX_3
            4 -> return MarkdownElementTypes.ATX_4
            5 -> return MarkdownElementTypes.ATX_5
            6 -> return MarkdownElementTypes.ATX_6
            else -> return MarkdownElementTypes.ATX_6
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine == -1) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.PROPAGATE)
        }
        return MarkerBlock.ProcessingResult.CANCEL
    }

}