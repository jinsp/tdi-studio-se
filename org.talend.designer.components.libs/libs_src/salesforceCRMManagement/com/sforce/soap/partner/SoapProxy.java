package com.sforce.soap.partner;

import java.rmi.RemoteException;

import com.sforce.soap.partner.InvalidateSessionsResult;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

public class SoapProxy implements com.sforce.soap.partner.Soap {
  private String _endpoint = null;
  private com.sforce.soap.partner.Soap soap = null;
  
  public SoapProxy() {
    _initSoapProxy();
  }
  
  public SoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initSoapProxy();
  }
  
  private void _initSoapProxy() {
    try {
      soap = (new com.sforce.soap.partner.SforceServiceLocator()).getSoap();
      if (soap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)soap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (soap != null)
      ((javax.xml.rpc.Stub)soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.sforce.soap.partner.Soap getSoap() {
    if (soap == null)
      _initSoapProxy();
    return soap;
  }
  
  public com.sforce.soap.partner.LoginResult login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.LoginFault{
    if (soap == null)
      _initSoapProxy();
    return soap.login(username, password);
  }
  
  public com.sforce.soap.partner.DescribeSObjectResult describeSObject(java.lang.String sObjectType) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeSObject(sObjectType);
  }
  
  public com.sforce.soap.partner.DescribeSObjectResult[] describeSObjects(java.lang.String[] sObjectType) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeSObjects(sObjectType);
  }
  
  public com.sforce.soap.partner.DescribeGlobalResult describeGlobal() throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeGlobal();
  }
  
  public com.sforce.soap.partner.DescribeLayoutResult describeLayout(java.lang.String sObjectType, java.lang.String[] recordTypeIds) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeLayout(sObjectType, recordTypeIds);
  }
  
  public com.sforce.soap.partner.DescribeSoftphoneLayoutResult describeSoftphoneLayout() throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeSoftphoneLayout();
  }
  
  public com.sforce.soap.partner.DescribeTabSetResult[] describeTabs() throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.describeTabs();
  }
  
  public com.sforce.soap.partner.SaveResult[] create(com.sforce.soap.partner.sobject.SObject[] sObjects) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.create(sObjects);
  }
  
  public com.sforce.soap.partner.SaveResult[] update(com.sforce.soap.partner.sobject.SObject[] sObjects) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.update(sObjects);
  }
  
  public com.sforce.soap.partner.UpsertResult[] upsert(java.lang.String externalIDFieldName, com.sforce.soap.partner.sobject.SObject[] sObjects) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.upsert(externalIDFieldName, sObjects);
  }
  
  public com.sforce.soap.partner.MergeResult[] merge(com.sforce.soap.partner.MergeRequest[] request) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.merge(request);
  }
  
  public com.sforce.soap.partner.DeleteResult[] delete(java.lang.String[] ids) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.delete(ids);
  }
  
  public com.sforce.soap.partner.UndeleteResult[] undelete(java.lang.String[] ids) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.undelete(ids);
  }
  
  public com.sforce.soap.partner.EmptyRecycleBinResult[] emptyRecycleBin(java.lang.String[] ids) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.emptyRecycleBin(ids);
  }
  
  public com.sforce.soap.partner.sobject.SObject[] retrieve(java.lang.String fieldList, java.lang.String sObjectType, java.lang.String[] ids) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.MalformedQueryFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.retrieve(fieldList, sObjectType, ids);
  }
  
  public com.sforce.soap.partner.ProcessResult[] process(com.sforce.soap.partner.ProcessRequest[] actions) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.process(actions);
  }
  
  public com.sforce.soap.partner.LeadConvertResult[] convertLead(com.sforce.soap.partner.LeadConvert[] leadConverts) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.convertLead(leadConverts);
  }
  
  public com.sforce.soap.partner.GetDeletedResult getDeleted(java.lang.String sObjectType, java.util.Calendar startDate, java.util.Calendar endDate) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault{
    if (soap == null)
      _initSoapProxy();
    return soap.getDeleted(sObjectType, startDate, endDate);
  }
  
  public com.sforce.soap.partner.GetUpdatedResult getUpdated(java.lang.String sObjectType, java.util.Calendar startDate, java.util.Calendar endDate) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault{
    if (soap == null)
      _initSoapProxy();
    return soap.getUpdated(sObjectType, startDate, endDate);
  }
  
  public com.sforce.soap.partner.QueryResult query(java.lang.String queryString) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.MalformedQueryFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.query(queryString);
  }
  
  public com.sforce.soap.partner.QueryResult queryAll(java.lang.String queryString) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.MalformedQueryFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.queryAll(queryString);
  }
  
  public com.sforce.soap.partner.QueryResult queryMore(java.lang.String queryLocator) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidQueryLocatorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.queryMore(queryLocator);
  }
  
  public com.sforce.soap.partner.SearchResult search(java.lang.String searchString) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.MalformedSearchFault, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidSObjectFault, com.sforce.soap.partner.fault.InvalidFieldFault{
    if (soap == null)
      _initSoapProxy();
    return soap.search(searchString);
  }
  
  public com.sforce.soap.partner.GetServerTimestampResult getServerTimestamp() throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.getServerTimestamp();
  }
  
  public com.sforce.soap.partner.SetPasswordResult setPassword(java.lang.String userId, java.lang.String password) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidIdFault, com.sforce.soap.partner.fault.InvalidNewPasswordFault{
    if (soap == null)
      _initSoapProxy();
    return soap.setPassword(userId, password);
  }
  
  public com.sforce.soap.partner.ResetPasswordResult resetPassword(java.lang.String userId) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault, com.sforce.soap.partner.fault.InvalidIdFault{
    if (soap == null)
      _initSoapProxy();
    return soap.resetPassword(userId);
  }
  
  public com.sforce.soap.partner.GetUserInfoResult getUserInfo() throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.getUserInfo();
  }
  
  public com.sforce.soap.partner.SendEmailResult[] sendEmail(com.sforce.soap.partner.Email[] messages) throws java.rmi.RemoteException, com.sforce.soap.partner.fault.UnexpectedErrorFault{
    if (soap == null)
      _initSoapProxy();
    return soap.sendEmail(messages);
  }
  public InvalidateSessionsResult[] invalidateSessions(String[] sessionIds) throws RemoteException, UnexpectedErrorFault {
      // TODO Auto-generated method stub
      return null;
  }

  public void logout() throws RemoteException, UnexpectedErrorFault {
      // TODO Auto-generated method stub

  }
  
}