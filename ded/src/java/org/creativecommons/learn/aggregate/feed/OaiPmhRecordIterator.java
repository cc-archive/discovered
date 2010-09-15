package org.creativecommons.learn.aggregate.feed;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.lang.NotImplementedException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

public class OaiPmhRecordIterator implements Iterator<Record> {
	private OaiPmhServer server;
	private static final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd");
	private LinkedList<Record> nextRecords = null;
	private ResumptionToken resumptionToken;

	public OaiPmhRecordIterator(OaiPmhServer server, MetadataFormat format, Date last_import_date) {
		// prepare the nextRecords list
		this.nextRecords = new LinkedList<Record>();

		// Store the server in an instance variable
		this.server = server;

		// Convert the date to a format that the server accepts
		String date_string;
		if (last_import_date == null) {
			date_string = null; // When this is null, we get everything.
		} else {
			date_string = iso8601.format(last_import_date);
		}
		
		// Try to get the RecordsList out of the server
		RecordsList records = null;
		
		try {
			records = server.listRecords(format.getPrefix(), date_string, null, null);
		} catch (OAIException e) {
			// If the server is not going to cooperate, we are not going
			// to have any Records either.
			return;
		}
		
		// Drain it into a normal List<Record>
		this.drainOneRecordsList(records);
	}
	
	@Override
	public boolean hasNext() {
		if (this.nextRecords.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * This takes the current RecordsList and copies its contents into a normal
	 * java List<Record>.
	 */
	private void drainOneRecordsList(RecordsList records) {
		/* Grab the data out of the RecordList */
		for (Record record : records.asList()) {
			this.nextRecords.add(record);
		}
		
		/* Stash the resumption token, if it exists,
		 * so we can get another RecordsList when necessary.
		 * 
		 * When this is null, then we know we are done.
		 */
		this.resumptionToken = records.getResumptionToken();
	}
	
	private void refillRecordsList() {
		if (this.resumptionToken == null) {
			// Then there is nothing to refill. Return now.
			return;
		}
		
		RecordsList toBeDrained;
		try {
			toBeDrained = this.server.listRecords(this.resumptionToken);
		} catch (OAIException e) {
			return;
		}
		this.drainOneRecordsList(toBeDrained);
	}

	@Override
	public Record next() {
		/* If there is a next, pop it off the front. */
		Record ret = this.nextRecords.removeFirst();
		
		/* If nextRecords is empty, refill it before we return.
		 * That way, calls to this.hasNext() will be accurate. */
		if (this.nextRecords.size() == 0) {
			this.refillRecordsList();
		}
		
		// Now we can return.
		return ret;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}
}
