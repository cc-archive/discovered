package org.creativecommons.delicious;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.DEdConfiguration;
import org.creativecommons.learn.oercloud.Resource;
import org.creativecommons.learn.plugin.MetadataRetriever;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DeliciousRetriever implements MetadataRetriever {

	public final static Log LOG = LogFactory.getLog(DeliciousRetriever.class);
	
	private HttpClient httpClient;
	private XPathFactory factory;
	private XPath xpath;
	private XPathExpression popularXPathExpr;
	private XPathExpression recommendedXPathExpr;
	private DocumentBuilder builder;
	
	private Configuration config = DEdConfiguration.create();
	private String deliciousUser = config.get("delicious.username");
	private String deliciousPass = config.get("delicious.password");
	

	public DeliciousRetriever() {

		httpClient = new HttpClient();
		httpClient.getParams().setAuthenticationPreemptive(true);
		Credentials defaultcreds = new UsernamePasswordCredentials(
				deliciousUser, deliciousPass);
		httpClient.getState().setCredentials(
				new AuthScope("api.del.icio.us", 443, AuthScope.ANY_REALM),
				defaultcreds);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.info("We didn't do the dom stuff because: " + e.getMessage());
		}

		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		
		try {
			popularXPathExpr = xpath.compile("//popular/text()");
			recommendedXPathExpr = xpath.compile("//recommended/text()");
		} catch (XPathExpressionException e) {
			LOG.info("OMG! How did we attempt to compile an invalid expression: "
							+ e.getMessage());
		}
	}

	@Override
	public Resource retrieve(Resource resource) {
		LOG.info("Called delicious retrieve for " + resource.getUrl());

		try {
			resource.getSubjects().addAll(getTags(resource.getUrl()));
		} catch (HttpException e) {
			LOG.info("Error retrieving tags from delicious for "
					+ resource.getUrl() + " because: " + e.getMessage());
		} catch (XPathExpressionException e) {
			LOG.info("Error retrieving tags from delicious for "
					+ resource.getUrl() + " because: " + e.getMessage());
		} catch (IOException e) {
			LOG.info("Error retrieving tags from delicious for "
					+ resource.getUrl() + " because: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			LOG.info("Error retrieving tags from delicious for "
					+ resource.getUrl() + " because: " + e.getMessage());
		} catch (SAXException e) {
			LOG.info("Error retrieving tags from delicious for "
					+ resource.getUrl() + " because: " + e.getMessage());
		}
		return resource;
	}

	public ArrayList<String> getTags(String targetURI) throws HttpException,
			IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {
		String queryUrl = "https://api.del.icio.us/v1/posts/suggest?url="
				+ targetURI;

		ArrayList<String> tagSet = new ArrayList<String>();
		GetMethod tagRequest = new GetMethod(queryUrl);

		try {
			httpClient.executeMethod(tagRequest);
			Document doc = builder.parse(tagRequest.getResponseBodyAsStream());
			
			// add all returned popular tags for the URL
			Object result = popularXPathExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				tagSet.add(nodes.item(i).getNodeValue());
			}
			
			// add all returned recommended tags for the URL
			result = recommendedXPathExpr.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				tagSet.add(nodes.item(i).getNodeValue());
			}

		}

		finally {
			tagRequest.releaseConnection();
		}
		return tagSet;
	}
}
