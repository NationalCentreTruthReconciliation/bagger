import os
import urllib
from transfer import utils, log

class TransferServices():
    def __init__(self, config):
        self.config = config
        self.project_name = "transfer-services-core"
        self.file_location = "files"
        self.debug = config['DEBUG'] if config['DEBUG'] else False        
        self.install_dir = config['TRANSFER_SERVICES_INSTALL_DIR'] if config['TRANSFER_SERVICES_INSTALL_DIR'] else "."        
        self.db_prefix = config['DB_PREFIX'] + "_" if config['DB_PREFIX'] else ''
        self.role_prefix = config['ROLE_PREFIX'] + "_" if config['ROLE_PREFIX'] else ''
        self.version = config['VERSION']
        self.db_name = ""
        self.passwds = {
            'transfer_user': config['TRANSFER_PASSWD'] if config['TRANSFER_PASSWD'] else "transfer_user",
            'service_request_broker_user': config['REQUEST_BROKER_PASSWD'] if config['REQUEST_BROKER_PASSWD'] else "service_request_broker_user",
        }
        self.driver_package = "%s/transfer-services-core-%s-bin.zip" % (self.file_location, self.version)
        self.url = 'https://beryllium.rdc.lctl.gov/trac/transfer/browser/trunk/transfer-services-core/release/transfer-services-core-%s-bin.zip?format=raw' % (self.version)
        self.driver_location = "%s/%s-%s/bin" % (self.install_dir, self.project_name, self.version)
        self.drivers = ("componentdriver","servicecontainerdriver")
        self.init_driver = "servicecontainer"
        self.component_location = "%s/%s-%s" % (self.install_dir, self.project_name, self.version)
        self.component_packages = {}
        for project in config['COMPONENT_PROJECTS']:
            trac_project = "" if project == "core" else project
            self.component_packages["%s/transfer-components-%s-%s-bin.zip" % (self.file_location, project, config['VERSION'])] = "https://beryllium.rdc.lctl.gov/trac/%stransfer/browser/trunk/transfer-components-%s/release/transfer-components-%s-%s-bin.zip?format=raw" % (trac_project, project, project, self.version)
        self.servicecontainer_props = """host=%s
                                         queues=%s
                                         jobtypes=%s
                                         delegatejobtypes=%s
                                      """ % (config['HOST'] if config['HOST'] else "localhost", config['QUEUES'] if config['QUEUES'] else "jobqueue", config['JOBTYPES'] if ['JOBTYPES'] else "test", config['DELEGATE_JOBTYPES'] if ['DELEGATE_JOBTYPES'] else "")

        self.servicecontainer_conf = "%s/%s-%s/conf/servicecontainer.properties" % (self.install_dir, self.project_name, self.version)
        self.service_conf =  "%s/%s-%s/conf/service.local.properties" % (self.install_dir, self.project_name, self.version)
        self.datasources_props = "%s/%s-%s/conf/datasources.properties" % (
            self.install_dir, self.project_name, self.version
        )
        component_selection = config['COMPONENT_SELECTION'] if config.has_key('COMPONENT_SELECTION') else {}
        self.component_select_props = ""        
        for component in component_selection.keys():
            self.component_select_props = "%s%s=%s\n" % (self.component_select_props, component, component_selection.get(component))
        self.components_conf = "%s/%s-%s/conf/components.local.properties" % (self.install_dir, self.project_name, self.version)
        self.components_props = config['COMPONENTS_PROPS'] if config.has_key('COMPONENTS_PROPS') else {}            
        self.db_server = config['PGHOST'] if config['PGHOST'] else 'localhost'
        self.db_port = config['PGPORT'] if config['PGPORT'] else '5432'
        self.user = config['USER'] if config['USER'] else 'transfer'
        self.group = config['GROUP'] if config['GROUP'] else 'transfer'
	self.run_number = config['RUN_NUMBER'] if config['RUN_NUMBER'] else '85'
        self.logger = log.Log(self.project_name)
        if not utils.check_java_home():
            self.logger.error("JAVA_HOME is not set properly: '%s'" % (os.environ['JAVA_HOME']))
            raise RuntimeError("JAVA_HOME is not set properly: '%s'" % (os.environ['JAVA_HOME']))        

    def start_container(self):
        """ starts up the service_container """
	utils.restart_container("/etc/init.d/%s" % (self.init_driver))
        return

    def deploy_drivers(self):
        """ deploys command-line drivers """
        if not os.path.exists(self.driver_package):
            urllib.urlretrieve(self.url, self.driver_package)
        for package in self.component_packages.keys():
            if not os.path.exists(package):
                urllib.urlretrieve(self.component_packages.get(package), package)
        utils.unzip(self.driver_package, self.install_dir, self.debug)
        for component_package in self.component_packages.keys():
            utils.unzip(component_package, self.component_location, self.debug)        
        for driver in self.drivers:
            utils.chmod("754", "%s/%s" % (self.driver_location, driver), self.debug)
        utils.setup_driver_init(self.init_driver, "servicecontainerdriver", self.driver_location, self.user, self.run_number)
        utils.localize_datasources_props(self.datasources_props, self.db_server, self.db_port, self.db_name, self.db_prefix, self.role_prefix, self.passwds, self.debug)
        utils.strtofile(self.servicecontainer_props, self.servicecontainer_conf, self.debug)
        utils.strtofile(self.component_select_props, self.service_conf, self.debug)
        utils.append_props(self.components_props, self.components_conf)
        utils.chown(self.user, self.group, "%s/%s-%s" % (self.install_dir, self.project_name, self.version), True)
        self.logger.info("Deploying %s drivers" % (self.project_name))
        return



