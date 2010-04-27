/*
 * License.java
 *
 * Copyright 2007, Creative Commons
 * licensed under the GNU LGPL License; see licenses/LICENSE for details
 *
 */

package org.creativecommons.license;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author nathan
 */
public class License {
    
    private String license_uri;
    private Store licenseStore;
    
    /**
     * Creates a new instance of License
     */
    public License(String license_uri) {
        
        this.license_uri = license_uri;
        this.licenseStore = Store.get();
    } 
    
    public String getLicenseUri() {
        return this.license_uri;
    }
    
    public String getName() {
        
        return this.licenseStore.literal(this.license_uri, DC.title, "en").getString() + " " +
                this.licenseStore.literal(this.license_uri, DCTerms.hasVersion, "").getString() + " " +
                this.getJurisdiction().getTitle();
    }
    
    public IJurisdiction getJurisdiction() {

        Resource jurisdiction = this.licenseStore.object(this.license_uri, CC.jurisdiction);
        
        if (jurisdiction != null)
            return new Jurisdiction(jurisdiction.getURI());
        
        return new Unported();

    } // getJurisdiction
    
    /**
     * Return the URL of the icon for this license if available; if unavailable,
     * return null.
     *
     * @return the URL for the license image
     *
     */
    public String getImageUrl() {
        
        return "http://i.creativecommons.org/l/" + this.getCode() + "/" + this.getVersion() + "/88x31.png";
    }
    
    /**
     * Return the license code for this License.  For example, the code for the
     * Attribution 3.0 license (http://creativecommons.org/licenses/by/3.0/) is
     * "by".  Note this is based on a Creative Commons-specific standard.
     *
     * @return license code for the selected License.
     */
    private String getCode() {
        
        try {
            URL licenseUrl = new URL(this.getLicenseUri());
            String[] pieces = licenseUrl.getPath().split("/");
            if (pieces.length > 2) {
                return pieces[2];
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String getVersion() {
        return this.licenseStore.literal(this.license_uri, DCTerms.hasVersion, "").getString();
    }
    
    public Boolean requireShareAlike() {
        
        return Boolean.valueOf(
                this.licenseStore.exists(this.license_uri, CC.requires, CC.ShareAlike)
                );
        
    }

    public Boolean prohibitCommercial() {
        
        return Boolean.valueOf(
                this.licenseStore.exists(this.license_uri, CC.prohibits, CC.CommercialUse)
                );

    }

    public Boolean allowRemix() {

        return Boolean.valueOf(
                this.licenseStore.exists(this.license_uri, CC.permits, CC.DerivativeWorks)
                );

    }

} // License
