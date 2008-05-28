package gov.loc.repository.workflow.actionhandlers;

import static gov.loc.repository.workflow.constants.FixtureConstants.QUEUE_1;
import static org.junit.Assert.*;
import gov.loc.repository.service.ServiceConstants;
import gov.loc.repository.serviceBroker.RespondingServiceBroker;
import gov.loc.repository.serviceBroker.ServiceRequest;
import gov.loc.repository.transfer.components.test.TestComponent;
import gov.loc.repository.utilities.ConfigurationFactory;
import gov.loc.repository.workflow.WorkflowConstants;
import gov.loc.repository.workflow.jbpm.spring.ContextService;

import java.lang.reflect.Proxy;

import org.apache.commons.configuration.Configuration;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:conf/servicerequestbroker-context.xml", "classpath:conf/servicerequestbroker-test-context.xml"})
public class ServiceInvocationHandlerTest {
	DummyActionHandler actionHandler= new DummyActionHandler(null);

	Configuration workflowConfig = ConfigurationFactory.getConfiguration(WorkflowConstants.PROPERTIES_NAME);
	Configuration servicesConfig = ConfigurationFactory.getConfiguration(ServiceConstants.PROPERTIES_NAME);
	
	@Autowired
	RespondingServiceBroker broker;

	protected HibernateTemplate template;
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.template = new HibernateTemplate(sessionFactory);	
	}
	
	@Before
	public void truncateDb()
	{
		this.template.execute(
				new HibernateCallback()
				{
					public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException
					{
						session.createSQLQuery("delete from string_entries").executeUpdate();
						session.createSQLQuery("delete from boolean_entries").executeUpdate();
						session.createSQLQuery("delete from integer_entries").executeUpdate();
						session.createSQLQuery("delete from service_request").executeUpdate();
						session.createSQLQuery("delete from service_container_registry").executeUpdate();
						return null;
					};
				}
		);
	}
		
	@Before
	public void setup() throws Exception
	{
		workflowConfig.clearProperty("none.TestComponent.queue");
		workflowConfig.addProperty("none.TestComponent.queue", QUEUE_1);
		
		ContextService contextService = new ContextService();		
		this.actionHandler.setApplicationContext(contextService.getContext());
	}
	
	@Test
	public void testInvoke() throws Exception {

		assertNull(broker.findAndAcknowledgeNextServiceRequest());
				
		TestComponent testComponent = actionHandler.createObject(TestComponent.class);
		assertTrue(Proxy.getInvocationHandler(testComponent) instanceof ServiceInvocationHandler);
		testComponent.test("foo", true, 1L);
		
		Thread.sleep(250);

		ServiceRequest req = broker.findAndAcknowledgeNextServiceRequest();
		assertNotNull(req);
		
		assertEquals("0", req.getCorrelationKey());
		assertEquals("test", req.getJobType());
		assertEquals("foo", req.getStringEntries().iterator().next().getValue());
		assertTrue(req.getBooleanEntries().iterator().next().getValue());
		assertEquals(Long.valueOf(1L), req.getIntegerEntries().iterator().next().getValue());
		
	}

	@Test
	public void testInvokeWithNull() throws Exception {

		assertNull(broker.findAndAcknowledgeNextServiceRequest());
				
		TestComponent testComponent = actionHandler.createObject(TestComponent.class);
		assertTrue(Proxy.getInvocationHandler(testComponent) instanceof ServiceInvocationHandler);
		testComponent.test(null, true, 1L);
		
		Thread.sleep(250);

		ServiceRequest req = broker.findAndAcknowledgeNextServiceRequest();
		assertNotNull(req);

		assertFalse(req.getStringEntries().isEmpty());
		assertNull(req.getStringEntries().iterator().next().getValue());
		
	}
	
}