package org.alfresco.training.lab7;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.training.lab7.FileUpload;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.BeanFactory;  
import org.springframework.beans.factory.xml.XmlBeanFactory;  
import org.springframework.core.io.ClassPathResource;  
import org.springframework.core.io.Resource;  

public class CustomFileUpload extends DeclarativeWebScript {
    private final String UPLOAD_FILE_PATH = "C:/Users/1039475/Desktop/abc.txt";
    private final String UPLOAD_DESTINATION = "D:/alfrescoj/ws4jb/alf_data_dev/contentstore/2016/4/27/15/28";
    protected ServiceRegistry serviceRegistry;

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    ApplicationContext context = new ClassPathXmlApplicationContext("service-context.xml");

    FileUpload objA = (FileUpload) context.getBean("fileupload");
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        File file = new File(UPLOAD_FILE_PATH);

        NodeRef parent = new NodeRef(UPLOAD_DESTINATION);
        String name = "name of file in Repository " + System.currentTimeMillis();
        System.out.println(""+name);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);

        
        NodeRef node = serviceRegistry.getNodeService().createNode(
                        parent,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                        ContentModel.TYPE_CONTENT, props).getChildRef();

        
        ContentWriter writer = serviceRegistry.getContentService().getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        String text = "";
        try {
            text = FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.putContent(text);

        Map<String, Object> model = new HashMap<String, Object>();
        if (status.getCode() == Status.STATUS_OK) {
            model.put("resultRepoWS", "File \"" + file.getName() + "\" uploaded successfully to the repository. Status: " + status.getCode());
            return model;
        } else {
            model.put("resultRepoWS", "There was an error while uploading document \"" + file.getName() + "\" - Status: " + status.getCode());
            return model;
        }
    }

   
    @SuppressWarnings("unused")
    private NodeRef getCompanyHome() {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        serviceRegistry.getSearchService();
        ResultSet rs = serviceRegistry.getSearchService().query(storeRef, SearchService.LANGUAGE_XPATH, "/app:company_home");
        NodeRef parent = null;
        try {
            if (rs.length() == 0) {
                throw new AlfrescoRuntimeException("Didn't find Company Home");
            }
            parent = rs.getNodeRef(0);
        } finally {
            rs.close();
        }
        return parent;
    }
}