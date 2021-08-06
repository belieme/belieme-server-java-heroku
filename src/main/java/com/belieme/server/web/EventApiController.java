package com.belieme.server.web;

import java.net.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ListResponse> getAllEventsFromDeptMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "studentId", required = false) String studentId) throws HttpException { //TODO studentId를 다른걸로 바꿀까?
        init(userToken, univCode, deptCode);
        
        checkRequesterHasPermissionToStudentIdFromDept(studentId);
        
        List<EventDto> eventList = getEventListFromDeptUsingStudentIdFilter(studentId);
        return createGetListResponseEntity(eventList);
    }

    @GetMapping("/things/{thingCode}/items/{itemNum}/events")
    public ResponseEntity<ListResponse> getAllEventsFromItemMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum) throws HttpException {
        init(userToken, univCode, deptCode);
        checkIfRequesterHasStaffPermission();
        
        List<EventDto> eventList = getEventListByThingCodeAndItemNumFromDept(thingCode, itemNum);
        return createGetListResponseEntity(eventList);
    }
    
    @GetMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}")
    public ResponseEntity<Response> getAnEventMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);
        
        EventDto event = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfRequesterHasPermissionToEvent(event);
        
        return createGetResponseEntity(event);
    }
    
    //TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/events/reserve")
    public ResponseEntity<Response> postRequestEventByUser(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws HttpException {
        init(userToken, univCode, deptCode);
        checkIfBodyIncludesThingCode(requestBody);
        checkIfRequesterHasUserPermission();
        
        String thingCode = requestBody.getThingCode();
        int itemNum = toInt(requestBody.getItemNum());
        String userId = requester.getStudentId();
        
        checkIfUserCanReserveThing(userId, thingCode);
        ItemDto reservedItem = getUsableItem(thingCode, itemNum);
        itemNum = reservedItem.getNum();
        
        EventDto event = createNewEventWithInitValue();
        event.setThingCode(requestBody.getThingCode());
        event.setItemNum(itemNum);
        event.setNum(getLastEventNumOfItem(thingCode, itemNum) + 1);
        event.setUserStudentId(userId);
        event.setReserveTimeStamp(System.currentTimeMillis()/1000);
        
        reservedItem.setLastEventNum(event.getNum());
        saveItem(reservedItem);
        
        event = saveEvent(event);
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + event.getThingCode() + "/items/" + event.getItemNum() + "/events/" + event.getNum();
        
        return createPostResponseEntity(location, event);
    }
    
    @PostMapping("/events/lost")
    public ResponseEntity<Response> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws HttpException {
        init(userToken, univCode, deptCode);
        checkIfRequesterHasStaffPermission();
        checkIfBodyIncludesThingCodeAndItemNum(requestBody);
        
        String thingCode = requestBody.getThingCode();
        int itemNum = toInt(requestBody.getItemNum());

        ItemDto lostItem = getItemIfIsUsable(thingCode, itemNum);
        
        EventDto event = createNewEventWithInitValue();
        event.setThingCode(thingCode);
        event.setItemNum(itemNum);
        event.setNum(getLastEventNumOfItem(thingCode, itemNum) + 1);
        event.setLostManagerStudentId(requester.getStudentId());
        event.setLostTimeStamp(System.currentTimeMillis()/1000);
        
        lostItem.setLastEventNum(event.getNum());
        saveItem(lostItem);
        
        event = saveEvent(event);
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + event.getThingCode() + "/items/" + event.getItemNum() + "/events/" + event.getNum();
        
        return createPostResponseEntity(location, event);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/cancel") //TODO 논의점 cancel manager를 만들어야 하는가?
    public ResponseEntity<Response> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);
        
        EventDto eventBeforeUpdate = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfRequesterHasPermissionToEvent(eventBeforeUpdate);
        checkIfStatusOfEventIsReserved(eventBeforeUpdate);
        
        eventBeforeUpdate.setCancelTimeStamp(System.currentTimeMillis()/1000);
        
        EventDto output = updateEvent(thingCode, itemNum, eventNum, eventBeforeUpdate);
        return createGetResponseEntity(output);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/approve")
    public ResponseEntity<Response> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        EventDto eventBeforeUpdate = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfStatusOfEventIsReserved(eventBeforeUpdate);
        
        eventBeforeUpdate.setApproveTimeStamp(System.currentTimeMillis()/1000);
        eventBeforeUpdate.setApproveManagerStudentId(requester.getStudentId());

        EventDto output = updateEvent(thingCode, itemNum, eventNum, eventBeforeUpdate);
        return createGetResponseEntity(output);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/return")
    public ResponseEntity<Response> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        EventDto eventBeforeUpdate = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfStatusOfEventIsUsingOrDelayed(eventBeforeUpdate);
            
        eventBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
        eventBeforeUpdate.setReturnManagerStudentId(requester.getStudentId());
                
        EventDto output = updateEvent(thingCode, itemNum, eventNum, eventBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/lost")
    public ResponseEntity<Response> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        EventDto eventBeforeUpdate = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfStatusOfEventIsUsingOrDelayed(eventBeforeUpdate);
            
        eventBeforeUpdate.setLostTimeStamp(System.currentTimeMillis()/1000);
        eventBeforeUpdate.setLostManagerStudentId(requester.getStudentId());
                
        EventDto output = updateEvent(thingCode, itemNum, eventNum, eventBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/events/{eventNum}/found")
    public ResponseEntity<Response> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int eventNum) throws HttpException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        EventDto eventBeforeUpdate = getEventByThingCodeAndItemNumAndEventNumFromDept(thingCode, itemNum, eventNum);
        checkIfStatusOfEventIsLost(eventBeforeUpdate);
            
        eventBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
        eventBeforeUpdate.setReturnManagerStudentId(requester.getStudentId());
                
        EventDto output = updateEvent(thingCode, itemNum, eventNum, eventBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    // TODO 1. 밑에 있는 해체하기 하기
    //      2. NotFoundOnDataBaseException, InternalDataBaseException을 InternalServerErrorException로 바꾸기
    
    private UserDto requester;
    private UniversityDto univ;
    private DepartmentDto dept;
    
    private void init(String userToken, String univCode, String deptCode) {
        checkUserTokenIsNotNull(userToken);
        requester = userDao.findByToken(userToken);
        univ = univDao.findByCode(univCode);
        dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    private EventDto createNewEventWithInitValue() {
        EventDto newEvent = new EventDto();
        
        newEvent.setUnivCode(univ.getCode());
        newEvent.setDeptCode(dept.getCode());
        newEvent.setThingCode(null);
        newEvent.setItemNum(0);
        newEvent.setNum(0);
        newEvent.setUserStudentId(null);
        newEvent.setApproveManagerStudentId(null);
        newEvent.setReturnManagerStudentId(null);
        newEvent.setLostManagerStudentId(null);
        newEvent.setReserveTimeStamp(0);
        newEvent.setApproveTimeStamp(0);
        newEvent.setReturnTimeStamp(0);
        newEvent.setCancelTimeStamp(0);
        newEvent.setLostTimeStamp(0);
        
        return newEvent;
    }
    
    private int toInt(Integer target) {
        if(target == null) {
            return 0;
        }
        return target.intValue();
    }
    
    private int getLastEventNumOfItem(String thingCode, int itemNum) {
        int lastEventNum = 0;
        List<EventDto> eventsByItem = getEventListByThingCodeAndItemNumFromDept(thingCode, itemNum);
        for(int i = 0; i < eventsByItem.size(); i++) {
            if(lastEventNum < eventsByItem.get(i).getNum()) {
                lastEventNum = eventsByItem.get(i).getNum();
            }
        }
        return lastEventNum;
    }
    
    private List<EventDto> getEventListFromDeptUsingStudentIdFilter(String userStudentId) {
        List<EventDto> eventList;
        if(userStudentId == null) {
            eventList = eventDao.findByUnivCodeAndDeptCode(univ.getCode(), dept.getCode());            
        } else {
            eventList = eventDao.findByUnivCodeAndDeptCodeAndUserId(univ.getCode(), dept.getCode(), userStudentId);
        }
        return eventList;
    }
    
    private List<EventDto> getEventListByThingCodeAndItemNumFromDept(String thingCode, int itemNum) {
        return eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univ.getCode(), dept.getCode(), thingCode, itemNum);
    }
    
    private EventDto getEventByThingCodeAndItemNumAndEventNumFromDept(String thingCode, int itemNum, int eventNum) {
        return eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univ.getCode(), dept.getCode(), thingCode, itemNum, eventNum);
    }
    
    private ItemDto getUsableItem(String thingCode, int itemNum) throws MethodNotAllowedException {
        if(itemNum == 0) {
            return getUsableItemFromThing(thingCode);
        } else {
            return getItemIfIsUsable(thingCode, itemNum);
        }
    }
    
    private ItemDto getUsableItemFromThing(String thingCode) throws MethodNotAllowedException {
        ItemDto output = null;
        List<ItemDto> itemsByThing = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univ.getCode(), dept.getCode(), thingCode);
        for(int i = 0; i < itemsByThing.size(); i++) {
            output = itemsByThing.get(i);
            if(getItemStatus(output) == ItemStatus.USABLE) {
                break;
            }
            output = null;
        }
        if(output == null) {
            throw new MethodNotAllowedException("이 thing은 사용할 수 없습니다.");
        }
        return output;
    }
    
    private ItemDto getItemIfIsUsable(String thingCode, int itemNum) throws MethodNotAllowedException {
        ItemDto output = itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(univ.getCode(), dept.getCode(), thingCode, itemNum);
        if(getItemStatus(output) != ItemStatus.USABLE) {
            throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
        }
        return output;
    }
    
    private ItemStatus getItemStatus(ItemDto itemDto) {
        int lastEventNum = itemDto.getLastEventNum();
        
        if(lastEventNum == 0) {
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
    
    private EventDto saveEvent(EventDto target) {
        return eventDao.save(target);
    }
    
    private EventDto updateEvent(String thingCode, int itemNum, int eventNum, EventDto target) {
        return eventDao.update(univ.getCode(), dept.getCode(), thingCode, itemNum, eventNum, target);
    }
    
    private ItemDto saveItem(ItemDto target) {
        return itemDao.save(target);
    }
    
    private void checkUserTokenIsNotNull(String userToken) throws UnauthorizedException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
    }
    
    private void checkIfBodyIncludesThingCode(ItemInfoJsonBody requestBody) throws BadRequestException {
        if(requestBody.getThingCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingCode(String), itemNum(int)(optional)");
        }
    }
    
    private void checkIfBodyIncludesThingCodeAndItemNum(ItemInfoJsonBody requestBody) throws BadRequestException {
        if(requestBody.getThingCode() == null || requestBody.getItemNum() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingCode(String), itemNum(int)(optional)");
        }
    }
    
    
    private void checkRequesterHasPermissionToStudentIdFromDept(String userStudentId) throws ForbiddenException {
        if(doesRequesterHaveStaffPermission()) {
            return;
        } else if(doesRequesterHaveUserPermission() && requester.getStudentId().equals(userStudentId)) {
            return;
        } else {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }
    
    private void checkIfRequesterHasPermissionToEvent(EventDto event) throws ForbiddenException {
        if(doesRequesterHaveStaffPermission()) {
            return;
        } else if(doesRequesterHaveUserPermission() && requester.getStudentId().equals(event.getUserStudentId())) {
            return;
        } else {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }
    
    private void checkIfRequesterHasUserPermission() throws ForbiddenException {
        if(!doesRequesterHaveUserPermission()) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }
    
    private boolean doesRequesterHaveUserPermission() {
        return requester.hasUserPermission(dept.getCode());
    }
    
    private void checkIfRequesterHasStaffPermission() throws ForbiddenException {
        if(doesRequesterHaveStaffPermission()) {
            return;
        } else {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }
    
    private boolean doesRequesterHaveStaffPermission() {
        return requester.hasStaffPermission(dept.getCode());
    }
    
    
    private void checkIfStatusOfEventIsReserved(EventDto event) throws MethodNotAllowedException {
        if(event.getStatus().equals("RESERVED")) {
           return;
        } else {
             throw new MethodNotAllowedException("'예약됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfStatusOfEventIsUsingOrDelayed(EventDto event) throws MethodNotAllowedException {
        if(event.getStatus().equals("USING") && !event.getStatus().equals("DELAYED")) {
            return; 
        } else {
            throw new MethodNotAllowedException("'사용중' 또는 '연체됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfStatusOfEventIsLost(EventDto event) throws MethodNotAllowedException {
        if(event.getStatus().equals("LOST")) {
            return;
        } else {
            throw new MethodNotAllowedException("'분실됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfUserCanReserveThing(String userStudentId, String thingCode) throws MethodNotAllowedException, InternalServerErrorException { // TODO 해체하기
        List<EventDto> eventListByUser;
        try {
            eventListByUser = eventDao.findByUnivCodeAndDeptCodeAndUserId(univ.getCode(), dept.getCode(), userStudentId);    
        } catch(InternalDataBaseException e) {
            throw new InternalServerErrorException("DataBase에 규칙을 어긴 record가 존재합니다.");
        }
        
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUser.size(); i++) {
            EventDto tmp = eventListByUser.get(i);
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getThingCode().equalsIgnoreCase(thingCode)) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                    throw new MethodNotAllowedException("빌리려고 하는 물품종류에 대한 열린 기록이 있습니다."); //TODO 이 exception은 없애던가 메세지를 바꾸기
                }
            }
        }
        if(currentEventCount >= 3) {
            throw new MethodNotAllowedException("사용자가 이미 3개의 물품을 사용 또는 예약하였습니다.");
        }
    }
    
    private ResponseEntity<Response> createGetResponseEntity(EventDto output) throws InternalServerErrorException {
        return ResponseEntity.ok().body(createResponse(univ, dept, output));
    }
    
    private ResponseEntity<Response> createPostResponseEntity(String location, EventDto output) throws InternalServerErrorException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(univ, dept, output));
    }
    
    private ResponseEntity<ListResponse> createGetListResponseEntity(List<EventDto> output) throws InternalServerErrorException {
        return ResponseEntity.ok().body(createListResponse(univ, dept, output));
    }
    
    private Response createResponse(UniversityDto univDto, DepartmentDto deptDto, EventDto eventDto) throws InternalServerErrorException {
        UniversityJsonBody univ = toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = toDepartmentJsonBody(deptDto);
        EventJsonBody event = toEventJsonBody(eventDto);
        return new Response(univ, dept, event);
    }
    
    private ListResponse createListResponse(UniversityDto univDto, DepartmentDto deptDto, List<EventDto> eventDtoList) throws InternalServerErrorException {
        UniversityJsonBody univ = toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = toDepartmentJsonBody(deptDto);
        List<EventJsonBody> eventList = new ArrayList<>();
        for(int i = 0; i < eventDtoList.size(); i++) {
            eventList.add(toEventJsonBody(eventDtoList.get(i)));
        }
        return new ListResponse(univ, dept, eventList);
    }
    
    private UniversityJsonBody toUniversityJsonBody(UniversityDto univDto) {
        return jsonBodyProjector.toUniversityJsonBody(univDto);
    }
    
    private DepartmentJsonBody toDepartmentJsonBody(DepartmentDto deptDto) throws InternalServerErrorException {
        try {
            return jsonBodyProjector.toDepartmentJsonBody(deptDto);
        } catch(InternalDataBaseException e) {
            throw new InternalServerErrorException("DataBase에 규칙을 어긴 record가 존재합니다.");
        }
    }
    
    private EventJsonBody toEventJsonBody(EventDto eventDto) throws InternalServerErrorException {
        try {
            return jsonBodyProjector.toEventJsonBody(eventDto);
        } catch(InternalDataBaseException e) {
            throw new InternalServerErrorException("DataBase에 규칙을 어긴 record가 존재합니다.");
        }
    }
    
    private URI createUri(String uri) throws InternalServerErrorException {
        URI location;
        try {
            location = new URI(uri);
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        return location;
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