package ksurfer.dataimporter;

import java.net.URL;

public class txtReaderConfig {

	/** Config key for the URL property. */
	static final String CFG_URL = "url";

	/**
	 */
	private URL m_url;
	/**
	 */
	private String m_colDelimiter;
	/**
	 */
	private String m_rowDelimiter;
	/**
	 */
	private String m_quoteString;
	/**
	 */
	private boolean m_hasRowHeader;
	/**
	 */
	private boolean m_hasColHeader;
	/**
	 */
	private String m_commentStart;
	/**
	 */
	private boolean m_supportShortLines;
	/**
	 */
	private long m_limitRowsCount;
	/**
	 */
	private int m_skipFirstLinesCount;
	/**
	 */
	private char m_dec_sep;

	/**
	 * Creates a new txtReaderConfig with default values for all settings except
	 * the url.
	 */
	public txtReaderConfig() {
		super();
		m_colDelimiter = "\t";
		m_rowDelimiter = "\n";
		m_quoteString = "\"";
		m_commentStart = "#";
		m_hasRowHeader = true;
		m_hasColHeader = true;
		m_supportShortLines = true;
		m_limitRowsCount = -1L;
		m_skipFirstLinesCount = -1;
		m_dec_sep = '.';

	}

	public char getDecSep() {
		return m_dec_sep;
	}

	/** @return the url */
	URL getUrl() {
		return m_url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	void setUrl(final URL url) {
		m_url = url;
	}

	/** @return the colDelimiter */
	public String getColDelimiter() {
		return m_colDelimiter;
	}

	/**
	 * @param colDelimiter
	 *            the colDelimiter to set
	 */
	void setColDelimiter(final String colDelimiter) {
		m_colDelimiter = colDelimiter;
	}

	/** @return the rowDelimiter */
	public String getRowDelimiter() {
		return m_rowDelimiter;
	}

	/**
	 * @param rowDelimiter
	 *            the rowDelimiter to set
	 */
	void setRowDelimiter(final String rowDelimiter) {
		m_rowDelimiter = rowDelimiter;
	}

	/** @return the quoteString */
	public String getQuoteString() {
		return m_quoteString;
	}

	/**
	 * @param quoteString
	 *            the quoteString to set
	 */
	void setQuoteString(final String quoteString) {
		m_quoteString = quoteString;
	}

	/** @return the hasRowHeader */
	public boolean hasRowHeader() {
		return m_hasRowHeader;
	}

	/**
	 * @param hasRowHeader
	 *            the hasRowHeader to set
	 */
	void setHasRowHeader(final boolean hasRowHeader) {
		m_hasRowHeader = hasRowHeader;
	}

	/** @return the hasColHeader */
	public boolean hasColHeader() {
		return m_hasColHeader;
	}

	/**
	 * @param hasColHeader
	 *            the hasColHeader to set
	 */
	void setHasColHeader(final boolean hasColHeader) {
		m_hasColHeader = hasColHeader;
	}

	/** @return the commentStart */
	public String getCommentStart() {
		return m_commentStart;
	}

	/**
	 * @param commentStart
	 *            the commentStart to set
	 */
	void setCommentStart(final String commentStart) {
		m_commentStart = commentStart;
	}

	/**
	 * @param supportShortLines
	 *            the supportShortLines to set
	 */
	void setSupportShortLines(final boolean supportShortLines) {
		m_supportShortLines = supportShortLines;
	}

	/** @return the supportShortLines */
	public boolean isSupportShortLines() {
		return m_supportShortLines;
	}

	/** @return the limitRowsCount (smaller 0 if unlimited). */
	public long getLimitRowsCount() {
		return m_limitRowsCount;
	}

	/**
	 * @param value
	 *            the limitRowsCount to set (smaller 0 if unlimited).
	 */
	void setLimitRowsCount(final long value) {
		m_limitRowsCount = value;
	}

	/** @return the skipFirstLinesCount (smaller 0 if none to skip). */
	public int getSkipFirstLinesCount() {
		return m_skipFirstLinesCount;
	}

	/**
	 * @param value
	 *            the skipFirstLinesCount to set (smaller 0 if none to skip).
	 */
	void setSkipFirstLinesCount(final int value) {
		m_skipFirstLinesCount = value;
	}

}
