/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.infostore.search.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.search.AndTerm;
import com.openexchange.groupware.infostore.search.ComparablePattern;
import com.openexchange.groupware.infostore.search.ComparisonType;
import com.openexchange.groupware.infostore.search.ContentTerm;
import com.openexchange.groupware.infostore.search.DescriptionTerm;
import com.openexchange.groupware.infostore.search.FileNameTerm;
import com.openexchange.groupware.infostore.search.FileSizeTerm;
import com.openexchange.groupware.infostore.search.OrTerm;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.search.TitleTerm;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link ToMySqlQueryVisitorTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ToMySqlQueryVisitorTest {
    private final static Pattern WRONG_OPERATORS = Pattern.compile("AND[ \t\r\n\f]*OR|AND[ \\t\\r\\n\\f]*AND|OR[ \\t\\r\\n\\f]*OR");
    private final static Pattern MULTIPLE_WHITESPACE = Pattern.compile("[ ]{2,}");

    private final ServerSession session;

    /**
     * Initializes a new {@link ToMySqlQueryVisitorTest}.
     */
    public ToMySqlQueryVisitorTest() {
        super();
        this.session = new SimServerSession(1, 1);
    }
    
    @Test
    public void testSqlPattern() throws Exception {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", true, true);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("UPPER(infostore_document.description) LIKE UPPER(?)"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));
        
        dtz = new DescriptionTerm("bluber blah?foo", false, true);
        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));
        
        dtz = new DescriptionTerm("*bluber %blah?foo*", false, false);
        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber \\%blah_foo%", visitor.previewParameters().get(0));
        
        dtz = new DescriptionTerm("bluber_blah", false, false);
        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description = ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "bluber_blah", visitor.previewParameters().get(0));
        
        dtz = new DescriptionTerm("bluber_blah", false, true);
        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber\\_blah%", visitor.previewParameters().get(0));
        
        dtz = new DescriptionTerm("bluber\\blah", false, false);
        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description = ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "bluber\\blah", visitor.previewParameters().get(0));
    }

    @Test
    public void testFolders() throws Exception {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] {120}, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.previewMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));
    }

    @Test
    public void testWithoutAllFolders() throws Exception {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, null, new int[] {120}, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));

        visitor = new ToMySqlQueryVisitor(session, new int[0], new int[] {120}, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));
    }

    @Test
    public void testWithoutOwnFolders() throws Exception {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, null, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));

        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[0], "SELECT field01");
        visitor.visit(dtz);
        result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore_document.description LIKE ?"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%bluber blah_foo%", visitor.previewParameters().get(0));
    }

    @Test
    public void testWithLimit() throws Exception {
        FileNameTerm term = new FileNameTerm("test123");
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", null, InfostoreSearchEngine.NOT_SET, 0, 5);
        visitor.visit(term);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("WHERE infostore.cid = 1 AND infostore.created_by=1 AND UPPER(infostore_document.filename) LIKE UPPER(?) LIMIT 0,5"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%test123%", visitor.previewParameters().get(0));
    }

    @Test
    public void testWithLimitAndOrder() throws Exception {
        FileNameTerm term = new FileNameTerm("test123");
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5);
        visitor.visit(term);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("WHERE infostore.cid = 1 AND infostore.created_by=1 AND UPPER(infostore_document.filename) LIKE UPPER(?) ORDER BY last_modified ASC LIMIT 0,5"));
        assertEquals("Unexpected nnumber of constant operators", 1, visitor.previewParameters().size());
        assertEquals("Unexpected constant operator", "%test123%", visitor.previewParameters().get(0));
    }

    // see bug 32874
    @Test
    public void testUnsupportedTerms() throws OXException {
        ContentTerm term1 = new ContentTerm("test123", false, false);
        FileSizeTerm term2 = new FileSizeTerm(new ComparablePattern<Number>() {

            @Override
            public Number getPattern() {
                return Integer.valueOf(1024 * 1024 * 10);
            }

            @Override
            public ComparisonType getComparisonType() {
                return ComparisonType.GREATER_THAN;
            }
        });
        List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
        terms.add(term1);
        terms.add(term2);
        OrTerm orTerm = new OrTerm(terms);
        AndTerm andTerm = new AndTerm(terms);

        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5);
        visitor.visit(andTerm);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());

        visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5);
        visitor.visit(orTerm);
        result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
    }

    private final String FULLTEXT_SEARCH_PATTERN = "fulltextSearchPattern";
    private final String MATCH_CLAUSE = "MATCH (title,description,filename,file_version_comment) AGAINST (? IN BOOLEAN MODE)";;
    @Test
    public void testFulltextSearch() throws Exception {
        List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
        terms.add(new FileNameTerm(FULLTEXT_SEARCH_PATTERN));
        terms.add(new TitleTerm(FULLTEXT_SEARCH_PATTERN, true, false));
        AndTerm term = new AndTerm(terms);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5, true, 2);
        visitor.visit(term);
        String result = visitor.previewMySqlQuery();
        assertFalse("Invalid SQL query", WRONG_OPERATORS.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", MULTIPLE_WHITESPACE.matcher(result).matches());
        assertTrue("No MATCH clause found in query", result.contains(MATCH_CLAUSE));
        int index = result.indexOf(MATCH_CLAUSE);
        assertNotEquals("MATCH clause for first SELECT clause not found", -1, index);
        index = result.indexOf(MATCH_CLAUSE, index + MATCH_CLAUSE.length());
        assertNotEquals("MATCH clause for second SELECT clause not found", -1, index);
        assertEquals("Fulltext search pattern in unexpected", FULLTEXT_SEARCH_PATTERN, visitor.previewFulltextSearchPattern());
    }

    private final String FULLTEXT_SEARCH_PATTERN_TOO_SHORT = "a";
    @Test
    public void testFulltextSearch_PatternTooShort() {
        List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
        terms.add(new FileNameTerm(FULLTEXT_SEARCH_PATTERN_TOO_SHORT));
        terms.add(new TitleTerm(FULLTEXT_SEARCH_PATTERN_TOO_SHORT, true, false));
        AndTerm term = new AndTerm(terms);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5, true, 3);
        try {
            term.visit(visitor);
        } catch (OXException e) {
            if (false == InfostoreExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.equals(e)) {
                fail("Wrong exception thrown: " + e.getMessage());
            }
        }
    }

    private final Pattern EXACT_MATCH_PATTERN = Pattern.compile("MATCH \\(.*?\\) AGAINST \\(.*? IN BOOLEAN MODE\\)", Pattern.DOTALL);
    @Test
    public void testExactMatch_omitFulltextIndex() throws Exception {
        FileNameTerm term = new FileNameTerm("exactMatch", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, new int[] { 119 }, new int[] { 120 }, "SELECT field01", Metadata.LAST_MODIFIED_LITERAL, InfostoreSearchEngine.ASC, 0, 5, true, 3);
        visitor.visit(term);
        String result = visitor.previewMySqlQuery();
        assertFalse("Fulltext index used even though search term is for exact match.", EXACT_MATCH_PATTERN.matcher(result).matches());
    }

}
