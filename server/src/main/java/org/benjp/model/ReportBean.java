package org.benjp.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.benjp.services.UserService;

import java.util.*;

public class ReportBean {
  HashMap<String, UserBean> attendees;
  List<String> questions;
  List<String> links;
  List<MessageBean> messages;
  List<FileBean> files;

  public ReportBean() {
    attendees = new HashMap<String, UserBean>();
    questions = new ArrayList<String>();
    links = new ArrayList<String>();
    messages = new ArrayList<MessageBean>();
    files = new ArrayList<FileBean>();
  }

  public Collection<UserBean> getAttendees() {
    return attendees.values();
  }

  public void addUser(UserBean attendee) {
    if (!attendees.containsKey(attendee.getName()))
    {
      attendee.setStatus(UserService.STATUS_OFFLINE);
      this.attendees.put(attendee.getName(), attendee);
    }
  }

  public void addAttendee(String user) {
    if (attendees.containsKey(user))
      this.attendees.get(user).setStatus(UserService.STATUS_AVAILABLE);
  }

  public List<String> getQuestions() {
    return questions;
  }

  public void addQuestion(String question) {
    this.questions.add(question);
  }

  public List<String> getLinks() {
    return links;
  }

  public void addLink(String link) {
    this.links.add(link);
  }

  public List<MessageBean> getMessages() {
    return messages;
  }

  public void addMessage(MessageBean message) {
    this.messages.add(0, message);
  }

  public List<FileBean> getFiles() {
    return files;
  }

  public void addFile(FileBean file) {
    this.files.add(file);
  }

  public void fill(BasicDBList messages, List<UserBean> users)
  {
    for (UserBean user:users)
    {
      addUser(user);
    }

    Iterator iterator = messages.iterator();
    while(iterator.hasNext())
    {
      BasicDBObject message = (BasicDBObject)iterator.next();
      String msg = message.get("message").toString();
      Long timestamp = (Long)message.get("timestamp");
      String user = message.get("user").toString();
      String fullname = message.get("fullname").toString();
      String email = message.get("email").toString();
      String date = message.get("date").toString();
      boolean isSystem = false;
      if (message.containsField("isSystem"))
        isSystem = "true".equals(message.get("isSystem").toString());
      BasicDBObject options = null;
      if (isSystem && message.containsField("options")) {
        options = (BasicDBObject)message.get("options");
        if (options.containsField("type")) {
          if ("type-link".equals(options.get("type").toString()))
          {
            addLink(options.get("link").toString());
            msg = "[ Link: "+ options.get("link").toString()+" ]";
          }
          else if ("type-question".equals(options.get("type").toString()))
          {
            addQuestion(msg);
            msg = "[ Question: "+ msg +" ]";
          }
          else if ("type-file".equals(options.get("type").toString()))
          {
            FileBean file = new FileBean();
            file.setName(options.get("name").toString());
            file.setUrl(options.get("restPath").toString());
            file.setSize(options.get("sizeLabel").toString());
            addFile(file);
            msg = "[ File: "+ options.get("name").toString()+" ]";
          }
          else if ("type-hand".equals(options.get("type").toString()))
          {
            msg = "[ Raised his hand ]";
          }
          else if ("call-join".equals(options.get("type").toString()))
          {
            msg = "[ Joined meeting ]";
          }
          else if ("call-on".equals(options.get("type").toString()))
          {
            msg = "[ Meeting started ]";
          }
          else if ("call-off".equals(options.get("type").toString()))
          {
            msg = "[ Meeting finished ]";
          }
        }

      }
      addAttendee(user);
      MessageBean messageBean = new MessageBean();
      messageBean.setDate(date);
      messageBean.setFullname(fullname);
      messageBean.setUser(user);
      messageBean.setMessage(msg);
      addMessage(messageBean);


      // timestamp, user, fullname, email, date, message, options, type, isSystem
    }
  }

  public String getAsHtml()
  {
    StringBuilder html = new StringBuilder();
    html.append("<div style='font-family: Lucida,arial,tahoma;'>");

    /**
     * Attendees
     */
    html.append("<span style='float:right; border: 1px solid #CCC;'>");
    html.append("  <div style='font-weight: bold;border-bottom: 1px solid #CCC;padding: 4px;font-size: larger'>Attendees</div>");
    for (UserBean userBean:this.getAttendees())
    {
      html.append("<div style='padding: 4px;'>");
      if ("available".equals(userBean.getStatus()))
        html.append("<span style='background-color: #3C3;color: white;padding: 1px 5px;'>o</span>");
      else
        html.append("<span style='background-color: #CCC;color: white;padding: 1px 5px;'>x</span>");
      html.append("  <span style='font-weight:bold;padding: 5px;'>").append(userBean.getFullname()).append("</span>");
      html.append("</div>");

    }
    html.append("</span>");

    /**
     * Meeting Notes
     */
    html.append("<h2>Meeting Notes</h2>");

    /**
     * Questions
     */
    html.append("<span style='border: 1px solid #CCC;width: 300px; display: inline-block;'>");
    html.append("  <div style='font-weight: bold;border-bottom: 1px solid #CCC;padding: 4px;font-size: larger'>Questions</div>");
    for (String question:this.getQuestions())
    {
      html.append("<div style='padding: 4px'>").append(question).append("</div>");
    }
    html.append("</span><br><br>");

    /**
     * Links
     */
    html.append("<span style='border: 1px solid #CCC;width: 300px; display: inline-block;'>");
    html.append("  <div style='font-weight: bold;border-bottom: 1px solid #CCC;padding: 4px;font-size: larger'>Links</div>");
    for (String link:this.getLinks())
    {
      html.append("<div style='padding: 4px'>");
      html.append("<span><a href='"+link+"'>").append(link).append("</a></span>");
      html.append("</div>");
    }
    html.append("</span><br><br>");

    /**
     * Files
     */
    html.append("<span style='border: 1px solid #CCC;width: 300px; display: inline-block;'>");
    html.append("  <div style='font-weight: bold;border-bottom: 1px solid #CCC;padding: 4px;font-size: larger'>Files</div>");
    for (FileBean file:this.getFiles())
    {
      html.append("<div style='padding: 4px'>");
      html.append("  <div><a href='http://demo.exoplatform.net"+file.getUrl().replaceFirst("/rest", "/rest/private")+"'>").append(file.getName()).append("</a></div>");
      html.append("  <div style='color: #ccc;'>").append(file.getSize()).append("</div>");
      html.append("</div>");
    }
    html.append("</span><br><br>");

    /**
     * Discussions
     */
    html.append("<h3>Discussions</h3>");
    html.append("<span>");
    String prevUser = "";
    for (MessageBean messageBean:this.getMessages())
    {
      html.append("<span style='padding: 4px; border-bottom: 1px dotted #DDD; width: 500px;display: block;'>");
      if (!messageBean.getUser().equals(prevUser))
      {
        html.append("  <div style='padding: 4px;color: #CCC;'>");
        html.append("    <span style='float: left; display: inline-block;padding-right: 10px;'><img src='http://demo.exoplatform.net:8080/rest/jcr/repository/social/production/soc:providers/soc:organization/soc:"+messageBean.getUser()+"/soc:profile/soc:avatar' width='30px' style='width:30px;'></span>");
        html.append("    <span style='width: 300px;display: inline-block;vertical-align: top;'>").append(messageBean.getFullname()).append("</span>");
        html.append("    <span style='font-size: smaller;vertical-align: top;'>").append(messageBean.getDate()).append("</span>");
        html.append("  </div>");
      }
      prevUser = messageBean.getUser();
      html.append("  <div style='padding: 0 4px; margin-left: 40px;vertical-align: top;'>").append(messageBean.getMessage()).append("</div>");
      html.append("</span>");
    }
    html.append("</span>");

    /**
     * closing
     */
    html.append("</div>");

    return html.toString();
  }

}