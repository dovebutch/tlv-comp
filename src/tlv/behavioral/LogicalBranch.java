/*
Copyright (c) 2014, Intel Corporation

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package tlv.behavioral;

import java.util.Vector;

import tlv.config.IdentifierType;
import tlv.parse.NodeType;
import tlv.parse.SourceFile;
import tlv.parse.identifier.Identifier;
import tlv.parse.identifier.ParseElement;
import tlv.parse.identifier.ParseMnemonic;
import tlv.parse.ParseBranch;

/**
 * The LogicalBranch class is primarily used as a class which a LogicalNode
 * class can point to as its parent. Classes which extend LogicalBranch include
 * LogicalStage, LogicalWhen, LogicalPipeline and LogicalBehHier. Currently all
 * these classes have corresponding ParseBranch classes, referred to by Vector
 * <ParseElement> identifiers.
 * 
 * @author ypyatnyc
 * 
 */
public class LogicalBranch extends LogicalNode {
	/**
	 * This string is used by a parent to identify this node as a child.
	 */
	private String label;

	/**
	 * The associated identifier. All corresponding ParseBranch.identifier must
	 * refer to the same identifier object.
	 */
	protected Identifier identifier;

	/**
	 * Provides a link to all instances where this LogicalBranch was declared in
	 * the original TLV code
	 */
	protected Vector<ParseElement> parse_elements;

	public String getLabel() {
		return label;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * Adds an identifier to the LogicalPipeline which means that the
	 * LogicalPipeline has an additional ParsePipeline reference associated with
	 * it
	 * 
	 * @param identifier
	 *            identifier linked to a ParsePipeline object
	 * @return true if the identifier was successfully added to the
	 *         LogicalPipeline
	 */
	public boolean addIdentifier(ParseMnemonic identifier) {
		if (label.compareTo(identifier.getLabel()) == 0) {
			parse_elements.add(identifier);
			label = identifier.getLabel();
			return true;
		} else
			return false;
	}

	/**
	 * Searches from self through parent ancestry for node of given type.
	 * 
	 * @param type
	 *            the type of LogicalBranch the method attempts to retrieve
	 * @return the closest LogicalBranch of NodeType type which exists in this
	 *         LogicalNode's ancestry (including self), or null if none.
	 */
	public LogicalBranch getSelfOrAncestorOfType(NodeType type) {
		LogicalBranch temp_logicalBranch = this;

		while (temp_logicalBranch != null) {
			if (temp_logicalBranch.getNodeType() == type)
				break;
			else
				temp_logicalBranch = temp_logicalBranch.getParent();
		}

		return temp_logicalBranch;
	}

	/**
	 * Searches from self through parent ancestry for a node whose type matches any of the given types.
	 * Usage: getSelfOrAncestorOfTypes((1 << NodeType.XXX.ordinal()) |
	 *                                 (1 << NodeType.XXX.ordinal()));
	 * 
	 * @param type
	 *            the type of LogicalBranch the method attempts to retrieve
	 * @return the LogicalBranch of NodeType type which exists in this
	 *         LogicalNode's ancestry (including self).
	 */
	public LogicalBranch getSelfOrAncestorOfTypes(int types) {
		LogicalBranch temp_logicalBranch = this;
		while (temp_logicalBranch != null) {
			if ((types & (1 << temp_logicalBranch.getNodeType().ordinal())) != 0)
				break;
			else
				temp_logicalBranch = temp_logicalBranch.getParent();
		}

		return temp_logicalBranch;
	}
	
	/**
	 * Searches from self through parent ancestry for a LogicalBehScope.
	 * @return
	 */
	public LogicalBehScope getSelfOrAncestorBehScope()
	{
		return (LogicalBehScope)getSelfOrAncestorOfTypes((1 << NodeType.BEH_HIER.ordinal()) |
			                                             (1 << NodeType.PIPELINE.ordinal())
			                                            );
	}
	
	// in the future a LogicalBranch might span files, in which case
	// the identifiers list will matter. Currently all identifiers will point
	// back to the same file
	public SourceFile getSourceFile() {
		ParseElement identifier = parse_elements.firstElement();
		SourceFile source_file = null;

		if (identifier != null) {
			source_file = (SourceFile) identifier
					.getParseBranch_ofType(NodeType.FILE);
		}

		return source_file;
	}

	/**
	 * Creates a LogicalBranch from a ParseElement identifier object.
	 * 
	 * @param identifier_
	 *            ParseElement derived from the newely constructed
	 *            LogicalBranch's associated ParseNode. Null for the top-level
	 *            implicit BEH_HIER only.
	 */
	public LogicalBranch(ParseElement identifier_) {
		super((identifier_ == null) ? null : identifier_.getLogicalBranch(),
				(identifier_ == null) ? NodeType.BEH_HIER : identifier_
						.getPrefix().getType());

		parse_elements = new Vector<ParseElement>(0, 0);
		parse_elements.add(identifier_);
		if (identifier_ == null)
		{
			label = "top";
			identifier = Identifier.rawConstructor(">top", IdentifierType.BEH_HIER);
		} else
		{
			label = identifier_.getLabel();
			identifier = (identifier_.getParseNode() instanceof ParseBranch) ? ((ParseBranch)(identifier_.getParseNode())).getIdentifier() : null;
		}
	}

	/**
	 * Creates an "dummy" LogicalBranch of specific NodeType and links it with a
	 * top LogicalBranch
	 * 
	 * @param logical_branch_
	 *            parent LogicalBranch object
	 * @param t
	 *            NodeType of the created LogicalBranch
	 */
	/*
	public LogicalBranch(LogicalBranch logical_branch_, NodeType t) {
		super(logical_branch_, t);

		parse_elements = new Vector<ParseElement>(0, 0);
	}
	*/

	
	public LogicalBranch(Identifier ident_, LogicalBranch logical_branch_, NodeType t) {
		super(logical_branch_, t);
		label = ident_.getName();
		identifier = ident_;
		parse_elements = new Vector<ParseElement>(0, 0);
	}
	
	/**
	 * Creates an "dummy" LogicalBranch and links it with a top LogicalBranch
	 * 
	 * @param logical_branch_
	 *            parent LogicalBranch object
	 */
	public LogicalBranch(LogicalBranch logical_branch_) {
		super(logical_branch_);

		parse_elements = new Vector<ParseElement>(0, 0);
	}
}
