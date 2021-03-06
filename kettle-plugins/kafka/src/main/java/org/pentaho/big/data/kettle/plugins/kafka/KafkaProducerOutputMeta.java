/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.kafka;

import java.util.List;
import java.util.Optional;
import org.pentaho.big.data.api.cluster.NamedClusterService;
import org.pentaho.big.data.api.cluster.service.locator.NamedClusterServiceLocator;
import org.pentaho.bigdata.api.jaas.JaasConfigService;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;
import org.w3c.dom.Node;

@Step( id = "KafkaProducerOutput", image = "KafkaProducerOutput.svg", name = "Kafka Producer",
  description = "Produce messages to a Kafka topic", categoryDescription = "Output" )
public class KafkaProducerOutputMeta extends BaseStepMeta implements StepMetaInterface {

  public static final String CLUSTER_NAME = "clusterName";
  public static final String CLIENT_ID = "clientId";
  public static final String TOPIC = "topic";
  public static final String KEY_FIELD = "keyField";
  public static final String MESSAGE_FIELD = "messageField";

  private static Class<?> PKG = KafkaProducerOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private String clusterName;
  private String clientId;
  private String topic;
  private String keyField;
  private String messageField;

  private NamedClusterService namedClusterService;
  private NamedClusterServiceLocator namedClusterServiceLocator;
  private MetastoreLocator metastoreLocator;

  public KafkaProducerOutputMeta() {
    super();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData( Node stepnode ) {
    setClusterName( XMLHandler.getTagValue( stepnode, CLUSTER_NAME ) );
    setClientId( XMLHandler.getTagValue( stepnode, CLIENT_ID ) );
    setTopic( XMLHandler.getTagValue( stepnode, TOPIC ) );
    setKeyField( XMLHandler.getTagValue( stepnode, KEY_FIELD ) );
    setMessageField( XMLHandler.getTagValue( stepnode, MESSAGE_FIELD ) );
  }

  @Override public void setDefault() {
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId stepId, List<DatabaseMeta> databases )
    throws KettleException {
    setClusterName( rep.getStepAttributeString( stepId, CLUSTER_NAME ) );
    setClientId( rep.getStepAttributeString( stepId, CLIENT_ID ) );
    setTopic( rep.getStepAttributeString( stepId, TOPIC ) );
    setKeyField( rep.getStepAttributeString( stepId, KEY_FIELD ) );
    setMessageField( rep.getStepAttributeString( stepId, MESSAGE_FIELD ) );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transformationId, ObjectId stepId )
    throws KettleException {
    rep.saveStepAttribute( transformationId, stepId, CLUSTER_NAME, clusterName );
    rep.saveStepAttribute( transformationId, stepId, CLIENT_ID, clientId );
    rep.saveStepAttribute( transformationId, stepId, TOPIC, topic );
    rep.saveStepAttribute( transformationId, stepId, KEY_FIELD, keyField );
    rep.saveStepAttribute( transformationId, stepId, MESSAGE_FIELD, messageField );
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
                                Trans trans ) {
    return new KafkaProducerOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new KafkaProducerOutputData();
  }

  public String getDialogClassName() {
    return "org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputDialog";
  }

  public String getBootstrapServers() {
    return Optional
        .ofNullable( namedClusterService.getNamedClusterByName( clusterName, metastoreLocator.getMetastore() ) )
        .map( nc -> nc.getKafkaBootstrapServers() ).orElse( "" );
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId( String clientId ) {
    this.clientId = clientId;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic( String topic ) {
    this.topic = topic;
  }

  public String getKeyField() {
    return keyField;
  }

  public void setKeyField( String keyField ) {
    this.keyField = keyField;
  }

  public String getMessageField() {
    return messageField;
  }

  public void setMessageField( String messageField ) {
    this.messageField = messageField;
  }

  @Override public String getXML() throws KettleException {
    StringBuilder retval = new StringBuilder();
    retval.append( "    " ).append( XMLHandler.addTagValue( CLUSTER_NAME, clusterName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( TOPIC, topic ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CLIENT_ID, clientId ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( KEY_FIELD, keyField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( MESSAGE_FIELD, messageField ) );
    return retval.toString();
  }

  public NamedClusterService getNamedClusterService() {
    return namedClusterService;
  }

  public MetastoreLocator getMetastoreLocator() {
    return metastoreLocator;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName( String clusterName ) {
    this.clusterName = clusterName;
  }

  public void setNamedClusterService( NamedClusterService namedClusterService ) {
    this.namedClusterService = namedClusterService;
  }

  public void setMetastoreLocator( MetastoreLocator metastoreLocator ) {
    this.metastoreLocator = metastoreLocator;
  }

  public NamedClusterServiceLocator getNamedClusterServiceLocator() {
    return namedClusterServiceLocator;
  }

  public void setNamedClusterServiceLocator(
    NamedClusterServiceLocator namedClusterServiceLocator ) {
    this.namedClusterServiceLocator = namedClusterServiceLocator;
  }

  public Optional<JaasConfigService> getJaasConfigService() {
    try {
      return Optional.ofNullable( namedClusterServiceLocator.getService(
        namedClusterService.getNamedClusterByName( getClusterName(), getMetastoreLocator().getMetastore() ),
        JaasConfigService.class ) );
    } catch ( Exception e ) {
      log.logDebug( "problem getting jaas config", e );
      return Optional.empty();
    }
  }
}
