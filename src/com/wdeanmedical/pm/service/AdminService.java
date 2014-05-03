/*
 * WDean Medical is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.wdeanmedical.com
 * copyright 2013-2014 WDean Medical
 */
 
package com.wdeanmedical.pm.service;


import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import com.wdeanmedical.pm.persistence.AdminDAO;
import com.wdeanmedical.pm.core.Core;
import com.wdeanmedical.pm.dto.AdminDTO;
import com.wdeanmedical.pm.entity.Clinician;
import com.wdeanmedical.pm.entity.User;
import com.wdeanmedical.pm.dto.BooleanResultDTO;
import com.wdeanmedical.pm.util.OneWayPasswordEncoder;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdminService {

  private static Log log = LogFactory.getLog(AppService.class);
  public static int RETURN_CODE_DUP_USERNAME = -1;
  public static int RETURN_CODE_INVALID_PASSWORD = -2;
  
  private ServletContext context;
  private WebApplicationContext wac;
  private AdminDAO adminDAO;


  public AdminService() throws MalformedURLException {
    context = Core.servletContext;
    wac = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
    adminDAO = (AdminDAO) wac.getBean("adminDAO");
  }
  

  public  void saveNewClinician(AdminDTO dto) throws Exception {
  
   if(adminDAO.checkUsername(dto.getUsername()) == false) {
      dto.setResult(false);
      dto.setErrorMsg("Username already in system");
      dto.setReturnCode(RETURN_CODE_DUP_USERNAME);
      return;
    }
    
    if (testPassword(dto.getPassword()) == false) {
      dto.setResult(false);
      dto.setErrorMsg("Insufficient Password");
      dto.setReturnCode(RETURN_CODE_INVALID_PASSWORD);
      return;
    }
    
    Clinician clinician = new Clinician();
    String salt = UUID.randomUUID().toString();
    clinician.setSalt(salt);
    String encodedPassword = OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), salt);
    clinician.setPassword(encodedPassword);
    
    clinician.setFirstName(dto.getFirstName());
    clinician.setMiddleName(dto.getMiddleName());
    clinician.setLastName(dto.getLastName());
    clinician.setUsername(dto.getUsername());
    clinician.setPrimaryPhone(dto.getPrimaryPhone());
    clinician.setSecondaryPhone(dto.getSecondaryPhone());
    clinician.setEmail(dto.getEmail());
    clinician.setPager(dto.getPager());
    clinician.setGroupName(dto.getGroupName());
    clinician.setPracticeName(dto.getPracticeName());
    clinician.setRole(adminDAO.findRoleById(dto.getRoleId()));
    clinician.setCredential(adminDAO.findCredentialById(dto.getCredentialId()));
    clinician.setActive(true);
    adminDAO.createClinician(clinician);
  }
  
  
  public boolean testPassword(String password) {
  
   if (password.length() < 6) {
    log.info("Submitted passwords is not at least six characters long");
    return false;
    }
    Pattern lowerCasePattern = Pattern.compile("[a-z]+");
    Matcher lowerCaseMatcher = lowerCasePattern.matcher(password);
        
    Pattern upperCasePattern = Pattern.compile("[A-Z]+");
    Matcher upperCaseMatcher = upperCasePattern.matcher(password);
        
    if (lowerCaseMatcher.find() == false || upperCaseMatcher.find() == false) {
      log.info("Sumitted passwords does not include at least one uppercase and one lowercase letter");
      return false;
    }
          
    Pattern numericPattern = Pattern.compile("\\d+");
    Matcher numericMatcher = numericPattern.matcher(password);
        
    Pattern punctuationPattern = Pattern.compile("\\p{Punct}+");
    Matcher punctuationMatcher = punctuationPattern.matcher(password);
         
    if (numericMatcher.find() == false || punctuationMatcher.find() == false) {
      log.info("Submitted passwords does not include at least one numeric character and one punctuation character");
      return false;
    }
    return true;
  }
  
  public BooleanResultDTO checkUserUsername(AdminDTO adminDTO) throws Exception {
    BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
    booleanResultDTO.setResult(adminDAO.checkUserUsername(adminDTO.getUsername()));
    return booleanResultDTO;
  }
  
  public BooleanResultDTO checkUsername(AdminDTO adminDTO) throws Exception {
    BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
    booleanResultDTO.setResult(adminDAO.checkUsername(adminDTO.getUsername()));
    return booleanResultDTO;
  }
  
  
  public void activateClinician(AdminDTO dto) throws Exception {
    Clinician clinician = adminDAO.findClinicianById(dto.getClinicianId());
    clinician.setActive(true);
    adminDAO.update(clinician);
  }
  
  
  public void deactivateClinician(AdminDTO dto) throws Exception {
    Clinician clinician = adminDAO.findClinicianById(dto.getClinicianId());
    clinician.setActive(false);
    adminDAO.update(clinician);
  }
  
  
  public void purgeClinician(AdminDTO dto) throws Exception {
    Clinician clinician = adminDAO.findClinicianById(dto.getClinicianId());
    clinician.setPurged(true);
    adminDAO.update(clinician);
  }
  
  
  public void updateClinician(AdminDTO dto) throws Exception {
    Clinician clinician = adminDAO.findClinicianById(dto.getClinicianId());
    String property = dto.getUpdateProperty();
    String value = dto.getUpdatePropertyValue();
    if (property.equals("firstName")) {clinician.setFirstName(value);} 
    else if (property.equals("middleName")) {clinician.setMiddleName(value);} 
    else if (property.equals("lastName")) {clinician.setLastName(value);} 
    else if (property.equals("email")) {clinician.setEmail(value);} 
    else if (property.equals("pager")) {clinician.setPager(value);} 
    else if (property.equals("primaryPhone")) {clinician.setPrimaryPhone(value);} 
    else if (property.equals("secondaryPhone")) {clinician.setSecondaryPhone(value);} 
    else if (property.equals("groupName")) {clinician.setGroupName(value);} 
    else if (property.equals("practiceName")) {clinician.setPracticeName(value);} 
    
    else if (property.equals("username")) {
     if(adminDAO.checkUsername(dto.getUsername()) == false) {
        dto.setResult(false);
        dto.setErrorMsg("Username already in system");
        dto.setReturnCode(RETURN_CODE_DUP_USERNAME);
        return;
      }
      clinician.setUsername(value);
    } 
    
    else if (property.equals("password")) {
      if (testPassword(dto.getPassword()) == false) {
        dto.setResult(false);
        dto.setErrorMsg("Insufficient Password");
        dto.setReturnCode(RETURN_CODE_INVALID_PASSWORD);
        return;
      }
      String salt = UUID.randomUUID().toString();
      clinician.setSalt(salt);
      String encodedPassword = OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), salt);
      clinician.setPassword(encodedPassword);
    } 
    
    adminDAO.update(clinician);
  }
  
  
  public  void saveNewUser(AdminDTO dto) throws Exception {
  
   if(adminDAO.checkUsername(dto.getUsername()) == false) {
      dto.setResult(false);
      dto.setErrorMsg("Username already in system");
      dto.setReturnCode(RETURN_CODE_DUP_USERNAME);
      return;
    }
    
    if (testPassword(dto.getPassword()) == false) {
      dto.setResult(false);
      dto.setErrorMsg("Insufficient Password");
      dto.setReturnCode(RETURN_CODE_INVALID_PASSWORD);
      return;
    }
    
    User user = new User();
    String salt = UUID.randomUUID().toString();
    user.setSalt(salt);
    String encodedPassword = OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), salt);
    user.setPassword(encodedPassword);
    
    user.setFirstName(dto.getFirstName());
    user.setMiddleName(dto.getMiddleName());
    user.setLastName(dto.getLastName());
    user.setUsername(dto.getUsername());
    user.setPrimaryPhone(dto.getPrimaryPhone());
    user.setSecondaryPhone(dto.getSecondaryPhone());
    user.setEmail(dto.getEmail());
    user.setPager(dto.getPager());
    user.setRole(adminDAO.findRoleById(dto.getRoleId()));
    user.setCredential(adminDAO.findCredentialById(dto.getCredentialId()));
    user.setActive(true);
    adminDAO.createUser(user);
  }
  
    
  public void activateUser(AdminDTO dto) throws Exception {
    User user = adminDAO.findUserById(dto.getUserId());
    user.setActive(true);
    adminDAO.update(user);
  }
  
  
  public void deactivateUser(AdminDTO dto) throws Exception {
    User user = adminDAO.findUserById(dto.getUserId());
    user.setActive(false);
    adminDAO.update(user);
  }
  
  
  public void purgeUser(AdminDTO dto) throws Exception {
    User user = adminDAO.findUserById(dto.getUserId());
    user.setPurged(true);
    adminDAO.update(user);
  }
  
  
  public void updateUser(AdminDTO dto) throws Exception {
    User user = adminDAO.findUserById(dto.getUserId());
    String property = dto.getUpdateProperty();
    String value = dto.getUpdatePropertyValue();
    if (property.equals("firstName")) {user.setFirstName(value);} 
    else if (property.equals("middleName")) {user.setMiddleName(value);} 
    else if (property.equals("lastName")) {user.setLastName(value);} 
    else if (property.equals("email")) {user.setEmail(value);} 
    else if (property.equals("pager")) {user.setPager(value);} 
    else if (property.equals("primaryPhone")) {user.setPrimaryPhone(value);} 
    else if (property.equals("secondaryPhone")) {user.setSecondaryPhone(value);} 
    
    else if (property.equals("username")) {
     if(adminDAO.checkUserUsername(dto.getUsername()) == false) {
        dto.setResult(false);
        dto.setErrorMsg("Username already in system");
        dto.setReturnCode(RETURN_CODE_DUP_USERNAME);
        return;
      }
      user.setUsername(value);
    } 
    
    else if (property.equals("password")) {
      if (testPassword(dto.getPassword()) == false) {
        dto.setResult(false);
        dto.setErrorMsg("Insufficient Password");
        dto.setReturnCode(RETURN_CODE_INVALID_PASSWORD);
        return;
      }
      String salt = UUID.randomUUID().toString();
      user.setSalt(salt);
      String encodedPassword = OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), salt);
      user.setPassword(encodedPassword);
    } 
    
    adminDAO.update(user);
  }

  public  List<User> getUsers(AdminDTO dto) throws Exception {
    return adminDAO.getUsers();
  }
}
