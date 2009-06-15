package gov.loc.repository.bagger.bag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagger.Contact;
import gov.loc.repository.bagger.Organization;

/**
 * Simple JavaBean domain object representing an organization.
 *
|    (Source-organization: California Digital Library                      )
|    (Organization-address: 415 20th Street, 4th Floor, Oakland, CA. 94612 )
|    (Contact-name: A. E. Newman                                           )
|    (Contact-phone: +1 510-555-1234                                       )
|    (Contact-email: alfred@ucop.edu                                       )
 *
 * @author Jon Steinbach
 */
public class BaggerProfile {
	private static final Log log = LogFactory.getLog(BaggerProfile.class);

	private BaggerOrganization sourceOrganization = new BaggerOrganization();
//	private Contact sourceContact = new Contact();
	private Contact toContact = new Contact();
	
	public BaggerOrganization getOrganization() {
		return this.sourceOrganization;
	}
	
	public void setOrganization(BaggerOrganization organization) {
		this.sourceOrganization = organization;
	}
	
	public Contact getSourceContact() {
		return this.sourceOrganization.getContact();
	}
	
	public void setSourceCountact(Contact contact) {
		this.sourceOrganization.setContact(contact);
	}
	
	public Contact getToContact() {
		return this.toContact;
	}
	
	public void setToContact(Contact contact) {
		this.toContact = contact;
	}

	public String getSourceOrganization() {
		return this.sourceOrganization.getSourceOrganization();
	}

	public void setSourceOrganization(String name) {
		this.sourceOrganization.setSourceOrganization(name);
	}

	public String getOrganizationAddress() {
		return this.sourceOrganization.getOrganizationAddress();
	}

	public void setOrganizationAddress(String address) {
		this.sourceOrganization.setOrganizationAddress(address);
	}

	public String getSrcContactName() {
		return this.sourceOrganization.getContact().getContactName();
	}

	public void setSrcContactName(String name) {
		this.sourceOrganization.getContact().setContactName(name);
	}

	public String getSrcContactPhone() {
		return this.sourceOrganization.getContact().getTelephone();
	}
	
	public void setSrcContactPhone(String phone) {
		this.sourceOrganization.getContact().setTelephone(phone);
	}

	public String getSrcContactEmail() {
		return this.sourceOrganization.getContact().getEmail();
	}
	
	public void setSrcContactEmail(String email) {
		this.sourceOrganization.getContact().setEmail(email);
	}

	public String getToContactName() {
		return this.toContact.getContactName();
	}

	public void setToContactName(String name) {
		this.toContact.setContactName(name);
	}

	public String getToContactPhone() {
		return this.toContact.getTelephone();
	}
	
	public void setToContactPhone(String phone) {
		this.toContact.setTelephone(phone);
	}

	public String getToContactEmail() {
		return this.toContact.getEmail();
	}
	
	public void setToContactEmail(String email) {
		this.toContact.setEmail(email);
	}
}