package gov.loc.repository.serviceBroker.impl;

import gov.loc.repository.serviceBroker.AbstractServiceBrokerTest;
import gov.loc.repository.serviceBroker.ServiceRequest;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServiceRequestImplTest extends AbstractServiceBrokerTest {
	
	@Test
	public void testServiceRequest()
	{		
		//A typical request/response pattern
		ServiceRequest req = serviveBrokerFactory.createServiceRequest(REQUESTER_1, "1", QUEUE_1, JOBTYPE_1);
		req.addString("key1", "value1");
		req.addString("key2", "value2");
		req.addString("key3", null);
		req.addInteger("key4", 1L);
		req.addInteger("key5", 2L);
		req.addInteger("key6", null);
		req.addBoolean("key7", true);
		req.addBoolean("key8", false);
		req.addBoolean("key9", null);		
		
		template.save(req);
		
		assertEquals(REQUESTER_1, req.getRequester());
		assertEquals("1", req.getCorrelationKey());
		assertEquals(QUEUE_1, req.getQueue());
		assertEquals(JOBTYPE_1, req.getJobType());
		assertNotNull(req.getRequestDate());
		assertEquals(3, req.getStringMap().size());
		assertEquals(3, req.getIntegerMap().size());
		assertEquals(3, req.getBooleanMap().size());
						
		req.acknowledgeRequest(RESPONDER_1);		
		template.saveOrUpdate(req);
		
		assertEquals(RESPONDER_1, req.getResponder());
		assertNotNull(req.getRequestAcknowledgedDate());
		
		req.respondFailure(new Exception("Darn"));		
		template.saveOrUpdate(req);
		
		assertFalse(req.isSuccess());
		assertNotNull(req.getErrorMessage());
		assertNotNull(req.getErrorDetail());
		
		req.acknowledgeResponse();		
		template.saveOrUpdate(req);
		
		assertNotNull(req.getResponseAcknowledgedDate());
		
	}
}
