package com.belieme.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.*;
import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.item.ItemDto;
import com.belieme.server.domain.item.ItemStatus;
import com.belieme.server.domain.event.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;


@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}")
public class EventApiController extends ApiController {
    @Autowired
    public EventApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }

    @GetMapping("/events")
    public ResponseEntity<ListResponse> getItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "studentId", required = false) String studentId) throws HttpException, ServerDomainException { //TODO value 바꾸기
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        UserDto user = userDao.findByToken(userToken); 
        if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && user.getStudentId().equals(studentId)))) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        List<EventDto> eventList;
        if(studentId == null) {
            eventList = eventDao.findByUnivCodeAndDeptCode(univCode, deptCode);            
        } else {
            eventList = eventDao.findByUnivCodeAndDeptCodeAndUserId(univCode, deptCode, studentId);
        }

        return ResponseEntity.ok().body(createListResponse(univ, dept, eventList));
    }

    @GetMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}")
    public ResponseEntity<Response> getItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        UserDto user = userDao.findByToken(userToken);
        
        EventDto event = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
        if(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && event.getUserStudentId() != null && event.getUserStudentId() == user.getStudentId())) {
            return ResponseEntity.ok().body(createResponse(univ, dept, event));
        }
        else {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }

    
    //TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/events/reserve")
    public ResponseEntity<Response> createRequestEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        String thingCode = requestBody.getThingCode();
        Integer itemNum = requestBody.getItemNum();
        if(thingCode == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingCode(String), itemNum(int)(optional)");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        List<EventDto> eventListByUser = eventDao.findByUnivCodeAndDeptCodeAndUserId(univCode, deptCode, user.getStudentId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUser.size(); i++) {
            EventDto tmp = eventListByUser.get(i);
            
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getThingCode() == thingCode) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                    throw new MethodNotAllowedException("빌리려고 하는 물품종류에 대한 열린 기록이 있습니다."); //TODO 이 exception은 없애던가 메세지를 바꾸기
                }
            }
        }
        if(currentEventCount >= 3) {
            throw new MethodNotAllowedException("사용자가 이미 3개의 물품을 사용 또는 예약하였습니다.");
        }
        
        ItemDto reservedItem = null;
        if(itemNum == null) {
            List<ItemDto> itemsByThing = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
            for(int i = 0; i < itemsByThing.size(); i++) {
                reservedItem = itemsByThing.get(i);
                if(getItemStatus(reservedItem) == ItemStatus.USABLE) {
                    break;
                }
                reservedItem = null;
            }
            if(reservedItem == null) {
                throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
            }
        } else {
            reservedItem = itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(univCode, deptCode, thingCode, itemNum);
            if(getItemStatus(reservedItem) != ItemStatus.USABLE) {
                throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
            }
        }
        
        if(eventDao == null) {
            System.out.println("설마");
        } else {
            System.out.println("역시");
        }
        
        //TODO univCode, deptCode, thingCode, itemNum 로그 다 찍어보고 어디서 null pointer exc 나오는지 확인하기
        List<EventDto> eventsByItem = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        int lastEventNum = 0;
        for(int i = 0; i < eventsByItem.size(); i++) {
            if(lastEventNum < eventsByItem.get(i).getNum()) {
                lastEventNum = eventsByItem.get(i).getNum();
            }
        }

        EventDto newEvent = new EventDto();
        
        newEvent.setUnivCode(univCode);
        newEvent.setDeptCode(deptCode);
        newEvent.setThingCode(thingCode);
        newEvent.setItemNum(reservedItem.getNum());
        newEvent.setNum(lastEventNum + 1);
        newEvent.setUserStudentId(user.getStudentId());
        newEvent.setApproveManagerStudentId(null);
        newEvent.setReturnManagerStudentId(null);
        newEvent.setLostManagerStudentId(null);
        newEvent.setReserveTimeStamp(System.currentTimeMillis()/1000);
        newEvent.setApproveTimeStamp(0);
        newEvent.setReturnTimeStamp(0);
        newEvent.setCancelTimeStamp(0);
        newEvent.setLostTimeStamp(0);
            
        EventDto output = eventDao.save(newEvent);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + output.getThingCode() + "/items/" + output.getItemNum() + "/events/" + output.getNum());
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        return ResponseEntity.created(location).body(createResponse(univ, dept, output));
    }
    
    private ItemStatus getItemStatus(ItemDto itemDto) throws ServerDomainException {
        int lastEventNum = itemDto.getLastEventNum();
        
        if(lastEventNum == 0) { // TODO default 이걸로??
            return ItemStatus.USABLE;
        }
    
        EventDto lastEvent;
        try {
            lastEvent = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastEventNum());    
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("EventApiController.getItemStatus()");//TODO 여기에 이게 있어도 되는가....
        }
        
        String lastEventStatus = lastEvent.getStatus();
        if(lastEventStatus.equals("EXPIRED")||lastEventStatus.equals("RETURNED")||lastEventStatus.equals("FOUND")||lastEventStatus.equals("FOUNDANDRETURNED")) {
            return ItemStatus.USABLE;
        }
        else if (lastEventStatus.equals("LOST")){
            return ItemStatus.INACTIVATE;
        } else {
            return ItemStatus.UNUSABLE;
        }
    }
    
    @PostMapping("/events/lost")
    public ResponseEntity<Response> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        String thingCode = requestBody.getThingCode();
        Integer itemNum = requestBody.getItemNum();
        if(thingCode == null || itemNum == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingId(int), itemNum(int)");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);

        ItemDto lostItem = itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(univCode, deptCode, thingCode, itemNum);
            
        if(getItemStatus(lostItem) != ItemStatus.USABLE) {
            throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
        }
        
        List<EventDto> eventsByItem = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode,deptCode, thingCode, itemNum);
        int lastEventNum = 0;
        for(int i = 0; i < eventsByItem.size(); i++) {
            if(lastEventNum < eventsByItem.get(i).getNum()) {
                lastEventNum = eventsByItem.get(i).getNum();
            }
        }
        
        EventDto newEvent = new EventDto();
        
        newEvent.setUnivCode(univCode);
        newEvent.setDeptCode(deptCode);
        newEvent.setThingCode(thingCode);
        newEvent.setItemNum(itemNum);
        newEvent.setNum(lastEventNum + 1);
        newEvent.setUserStudentId(null);
        newEvent.setApproveManagerStudentId(null);
        newEvent.setReturnManagerStudentId(null);
        newEvent.setLostManagerStudentId(user.getStudentId());
        newEvent.setReserveTimeStamp(0);
        newEvent.setApproveTimeStamp(0);
        newEvent.setReturnTimeStamp(0);
        newEvent.setCancelTimeStamp(0);
        newEvent.setLostTimeStamp(System.currentTimeMillis()/1000);
        
        EventDto output = eventDao.save(newEvent);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + output.getThingCode() + "/items/" + output.getItemNum() + "/events/" + output.getNum());
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        return ResponseEntity.created(location).body(createResponse(univ, dept, output));
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/cancel") //TODO 논의점 cancel manager를 만들어야 하는가?
    public ResponseEntity<Response> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        EventDto eventBeforeUpdate = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
        if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && eventBeforeUpdate.getUserStudentId() == user.getStudentId()))) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
            
        if(eventBeforeUpdate.getStatus().equals("RESERVED")) {
            eventBeforeUpdate.setCancelTimeStamp(System.currentTimeMillis()/1000);
                
            EventDto output = eventDao.update(univCode, deptCode, thingCode, itemNum, eventNum, eventBeforeUpdate);
            return ResponseEntity.ok().body(createResponse(univ, dept, output));
        }
        else {
                throw new MethodNotAllowedException("'예약됨'기록이 아닌 기록을 취소할 수 없습니다.");
        }
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/approve")
    public ResponseEntity<Response> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        EventDto eventBeforeUpdate = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
            
        if(eventBeforeUpdate.getStatus().equals("RESERVED")) {
            eventBeforeUpdate.setApproveTimeStamp(System.currentTimeMillis()/1000);
            eventBeforeUpdate.setApproveManagerStudentId(user.getStudentId());
                
            EventDto output = eventDao.update(univCode, deptCode, thingCode, itemNum, eventNum, eventBeforeUpdate);
            return ResponseEntity.ok().body(createResponse(univ, dept, output));
        }
        else {
            throw new MethodNotAllowedException("'예약됨'기록이 아닌 기록을 승인할 수 없습니다.");
        }
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/return")
    public ResponseEntity<Response> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        EventDto eventBeforeUpdate = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
            
        if(eventBeforeUpdate.getStatus().equals("USING") || eventBeforeUpdate.getStatus().equals("DELAYED")) {
            eventBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
            eventBeforeUpdate.setReturnManagerStudentId(user.getStudentId());
                
            EventDto output = eventDao.update(univCode, deptCode, thingCode, itemNum, eventNum, eventBeforeUpdate);
            return ResponseEntity.ok().body(createResponse(univ, dept, output));
        }
        else {
            throw new MethodNotAllowedException("'사용중' 또는 '연체됨' 기록이 아닌 기록을 반납 처리할 수 없습니다.");
        }
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/lost")
    public ResponseEntity<Response> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        EventDto eventBeforeUpdate = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
            
        if(eventBeforeUpdate.getStatus().equals("USING") || eventBeforeUpdate.getStatus().equals("DELAYED")) {
            eventBeforeUpdate.setLostTimeStamp(System.currentTimeMillis()/1000);
            eventBeforeUpdate.setLostManagerStudentId(user.getStudentId());
                
            EventDto output = eventDao.update(univCode, deptCode, thingCode, itemNum, eventNum, eventBeforeUpdate);
            return ResponseEntity.ok().body(createResponse(univ, dept, output));
        }
        else {
            throw new MethodNotAllowedException("'사용중' 또는 '연체됨' 기록이 아닌 기록을 반납 처리할 수 없습니다.");
        }
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/found")
    public ResponseEntity<Response> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UserDto user = userDao.findByToken(userToken);
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        EventDto eventBeforeUpdate = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
            
        if(eventBeforeUpdate.getStatus().equals("LOST")) {
            eventBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
            eventBeforeUpdate.setReturnManagerStudentId(user.getStudentId());
                
            EventDto output = eventDao.update(univCode, deptCode, thingCode, itemNum, eventNum, eventBeforeUpdate);
            return ResponseEntity.ok().body(createResponse(univ, dept, output));
        }
        else {
            throw new MethodNotAllowedException("'분실됨' 기록이 아닌 기록을 찾음 처리할 수 없습니다."); // TODO message가 구리네...
        }
    }
    
    private Response createResponse(UniversityDto univDto, DepartmentDto deptDto, EventDto eventDto) throws ServerDomainException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDto);
        EventJsonBody event = jsonBodyProjector.toEventJsonBody(eventDto);
        return new Response(univ, dept, event);
    }
    
    private ListResponse createListResponse(UniversityDto univDto, DepartmentDto deptDto, List<EventDto> eventDtoList) throws ServerDomainException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDto);
        List<EventJsonBody> eventList = new ArrayList<>();
        for(int i = 0; i < eventDtoList.size(); i++) {
            eventList.add(jsonBodyProjector.toEventJsonBody(eventDtoList.get(i)));
        }
        return new ListResponse(univ, dept, eventList);
    }
    
    public class Response {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        EventJsonBody event;

        public Response(UniversityJsonBody university, DepartmentJsonBody department, EventJsonBody event) {
            this.university = university;
            this.department = department;
            this.event = event;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }
        
        public EventJsonBody getEvent() {
            return event;
        }
    }
    
    public class ListResponse {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        List<EventJsonBody> events;

        public ListResponse(UniversityJsonBody university, DepartmentJsonBody department, List<EventJsonBody> events) {
            this.university = university;
            this.department = department;
            this.events = events;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }

        public List<EventJsonBody> getEvents() {
            return events;
        }
    }
}