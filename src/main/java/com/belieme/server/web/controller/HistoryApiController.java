package com.belieme.server.web.controller;

import java.net.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.history.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.history.*;

import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;


@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}")
public class HistoryApiController extends ApiController {    
    @Autowired
    public HistoryApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, HistoryRepository historyRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, historyRepo);
    }

    @GetMapping("/histories")
    public ResponseEntity<ListResponse> getAllHistoriesFromDeptMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "studentId", required = false) String studentId) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException { // TODO studentId를 다른걸로 바꿀까?
        init(userToken, univCode, deptCode);
        
        checkRequesterHasPermissionToStudentIdFromDept(studentId);
        
        List<HistoryDto> historyList = getHistoryListFromDeptUsingStudentIdFilter(studentId);
        return createGetListResponseEntity(historyList);
    }

    @GetMapping("/things/{thingCode}/items/{itemNum}/histories")
    public ResponseEntity<ListResponse> getAllHistoriesFromItemMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException {
        init(userToken, univCode, deptCode);
        checkIfRequesterHasStaffPermission();
        
        List<HistoryDto> historyList = getHistoryListByThingCodeAndItemNumFromDept(thingCode, itemNum);
        return createGetListResponseEntity(historyList);
    }
    
    @GetMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}")
    public ResponseEntity<Response> getAnHistoryMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException {
        init(userToken, univCode, deptCode);
        
        HistoryDto history = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfRequesterHasPermissionToHistory(history);
        
        return createGetResponseEntity(history);
    }

    // TODO reserve의 item정보를 pathVariable로 줄지 requestBody로 줄지 생각
    // TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/histories/reserve")
    public ResponseEntity<Response> postRequestHistoryByUser(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws UnauthorizedException, NotFoundException, InternalServerErrorException, BadRequestException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);
        checkIfBodyIncludesThingCode(requestBody);
        checkIfRequesterHasUserPermission();
        
        String thingCode = requestBody.getThingCode();
        int itemNum = toInt(requestBody.getItemNum());
        String userId = requester.getStudentId();
        
        checkIfUserCanReserveThing(userId, thingCode);
        ItemDto reservedItem = getUsableItem(thingCode, itemNum);
        itemNum = reservedItem.getNum();
        
        HistoryDto history = createNewHistoryWithInitValue();
        history.setThingCode(requestBody.getThingCode());
        history.setItemNum(itemNum);
        history.setNum(getLastHistoryNumOfItem(thingCode, itemNum) + 1);
        history.setUserStudentId(userId);
        history.setReserveTimeStamp(System.currentTimeMillis()/1000);
        
        history = saveHistory(history);
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + history.getThingCode() + "/items/" + history.getItemNum() + "/histories/" + history.getNum();
        
        return createPostResponseEntity(location, history);
    }
    
    @PostMapping("/histories/lost")
    public ResponseEntity<Response> createLostHistory(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ItemInfoJsonBody requestBody) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, BadRequestException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);
        checkIfRequesterHasStaffPermission();
        checkIfBodyIncludesThingCodeAndItemNum(requestBody);
        
        String thingCode = requestBody.getThingCode();
        int itemNum = toInt(requestBody.getItemNum());

        ItemDto lostItem = getItemIfIsUsable(thingCode, itemNum);
        
        HistoryDto history = createNewHistoryWithInitValue();
        history.setThingCode(thingCode);
        history.setItemNum(itemNum);
        history.setNum(getLastHistoryNumOfItem(thingCode, itemNum) + 1);
        history.setLostManagerStudentId(requester.getStudentId());
        history.setLostTimeStamp(System.currentTimeMillis()/1000);
        
        history = saveHistory(history);
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + history.getThingCode() + "/items/" + history.getItemNum() + "/histories/" + history.getNum();
        
        return createPostResponseEntity(location, history);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}/cancel") //TODO 논의점 cancel manager를 만들어야함...(5)
    public ResponseEntity<Response> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);
        
        HistoryDto historyBeforeUpdate = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfRequesterHasPermissionToHistory(historyBeforeUpdate);
        checkIfStatusOfHistoryIsReserved(historyBeforeUpdate);
        
        historyBeforeUpdate.setCancelTimeStamp(System.currentTimeMillis()/1000);
        
        HistoryDto output = updateHistory(thingCode, itemNum, historyNum, historyBeforeUpdate);
        return createGetResponseEntity(output);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}/approve")
    public ResponseEntity<Response> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        HistoryDto historyBeforeUpdate = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfStatusOfHistoryIsReserved(historyBeforeUpdate);
        
        historyBeforeUpdate.setApproveTimeStamp(System.currentTimeMillis()/1000);
        historyBeforeUpdate.setApproveManagerStudentId(requester.getStudentId());

        HistoryDto output = updateHistory(thingCode, itemNum, historyNum, historyBeforeUpdate);
        return createGetResponseEntity(output);
    }

    @PatchMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}/return")
    public ResponseEntity<Response> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        HistoryDto historyBeforeUpdate = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfStatusOfHistoryIsUsingOrDelayed(historyBeforeUpdate);
            
        historyBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
        historyBeforeUpdate.setReturnManagerStudentId(requester.getStudentId());
                
        HistoryDto output = updateHistory(thingCode, itemNum, historyNum, historyBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}/lost")
    public ResponseEntity<Response> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);        
        checkIfRequesterHasStaffPermission();
    
        HistoryDto historyBeforeUpdate = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfStatusOfHistoryIsUsingOrDelayed(historyBeforeUpdate);
            
        historyBeforeUpdate.setLostTimeStamp(System.currentTimeMillis()/1000);
        historyBeforeUpdate.setLostManagerStudentId(requester.getStudentId());
                
        HistoryDto output = updateHistory(thingCode, itemNum, historyNum, historyBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    @PatchMapping("/things/{thingCode}/items/{itemNum}/histories/{historyNum}/found")
    public ResponseEntity<Response> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum, @PathVariable int historyNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);    
        checkIfRequesterHasStaffPermission();
    
        HistoryDto historyBeforeUpdate = getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(thingCode, itemNum, historyNum);
        checkIfStatusOfHistoryIsLost(historyBeforeUpdate);
            
        historyBeforeUpdate.setReturnTimeStamp(System.currentTimeMillis()/1000);
        historyBeforeUpdate.setReturnManagerStudentId(requester.getStudentId());
                
        HistoryDto output = updateHistory(thingCode, itemNum, historyNum, historyBeforeUpdate);
        return createGetResponseEntity(output);
    }
    
    private UserDto requester;
    private UniversityDto univ;
    private DepartmentDto dept;
    
    private void init(String userToken, String univCode, String deptCode) throws UnauthorizedException, NotFoundException, InternalServerErrorException {
        requester = dataAdapter.findUserByToken(userToken);
        univ = dataAdapter.findUnivByCode(univCode);
        dept = dataAdapter.findDeptByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    private HistoryDto createNewHistoryWithInitValue() {
        HistoryDto newHistory = new HistoryDto();
        
        newHistory.setUnivCode(univ.getCode());
        newHistory.setDeptCode(dept.getCode());
        newHistory.setThingCode(null);
        newHistory.setItemNum(0);
        newHistory.setNum(0);
        newHistory.setUserStudentId(null);
        newHistory.setApproveManagerStudentId(null);
        newHistory.setReturnManagerStudentId(null);
        newHistory.setLostManagerStudentId(null);
        newHistory.setReserveTimeStamp(0);
        newHistory.setApproveTimeStamp(0);
        newHistory.setReturnTimeStamp(0);
        newHistory.setCancelTimeStamp(0);
        newHistory.setLostTimeStamp(0);
        
        return newHistory;
    }
    
    private int toInt(Integer target) {
        if(target == null) {
            return 0;
        }
        return target.intValue();
    }
    
    private int getLastHistoryNumOfItem(String thingCode, int itemNum) throws InternalServerErrorException {
        int lastHistoryNum = 0;
        List<HistoryDto> historiesByItem = getHistoryListByThingCodeAndItemNumFromDept(thingCode, itemNum);
        for(int i = 0; i < historiesByItem.size(); i++) {
            if(lastHistoryNum < historiesByItem.get(i).getNum()) {
                lastHistoryNum = historiesByItem.get(i).getNum();
            }
        }
        return lastHistoryNum;
    }
    
    private List<HistoryDto> getHistoryListFromDeptUsingStudentIdFilter(String userStudentId) throws InternalServerErrorException {
        List<HistoryDto> historyList;
        if(userStudentId == null) {
            historyList = dataAdapter.findAllHistoriesByUnivCodeAndDeptCode(univ.getCode(), dept.getCode());            
        } else {
            historyList = dataAdapter.findAllHistoriesByUnivCodeAndDeptCodeAndUserId(univ.getCode(), dept.getCode(), userStudentId);
        }
        return historyList;
    }
    
    private List<HistoryDto> getHistoryListByThingCodeAndItemNumFromDept(String thingCode, int itemNum) throws InternalServerErrorException {
        return dataAdapter.findAllHistoriesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univ.getCode(), dept.getCode(), thingCode, itemNum);
    }
    
    private HistoryDto getHistoryByThingCodeAndItemNumAndHistoryNumFromDept(String thingCode, int itemNum, int historyNum) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findHistoryByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univ.getCode(), dept.getCode(), thingCode, itemNum, historyNum);
    }
    
    private ItemDto getUsableItem(String thingCode, int itemNum) throws InternalServerErrorException, NotFoundException, MethodNotAllowedException {
        if(itemNum == 0) {
            return getUsableItemFromThing(thingCode);
        } else {
            return getItemIfIsUsable(thingCode, itemNum);
        }
    }
    
    private ItemDto getUsableItemFromThing(String thingCode) throws InternalServerErrorException, NotFoundException, MethodNotAllowedException {
        ItemDto output = null;
        List<ItemDto> itemsByThing = dataAdapter.findAllItemsByUnivCodeAndDeptCodeAndThingCode(univ.getCode(), dept.getCode(), thingCode);
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
    
    private ItemDto getItemIfIsUsable(String thingCode, int itemNum) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException {
        ItemDto output = dataAdapter.findItemByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univ.getCode(), dept.getCode(), thingCode, itemNum);
        if(getItemStatus(output) != ItemStatus.USABLE) {
            throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
        }
        return output;
    }
    
    private ItemStatus getItemStatus(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        int lastHistoryNum = itemDto.getLastHistoryNum();
        
        if(lastHistoryNum == 0) {
            return ItemStatus.USABLE;
        }
    
        HistoryDto lastHistory = dataAdapter.findHistoryByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastHistoryNum());    
        
        String lastHistoryStatus = lastHistory.getStatus();
        if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")||lastHistoryStatus.equals("FOUND")||lastHistoryStatus.equals("FOUNDANDRETURNED")) {
            return ItemStatus.USABLE;
        }
        else if (lastHistoryStatus.equals("LOST")){
            return ItemStatus.INACTIVATE;
        } else {
            return ItemStatus.UNUSABLE;
        }
    }
    
    private HistoryDto saveHistory(HistoryDto target) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
        return dataAdapter.saveHistory(target);
    }
    
    private HistoryDto updateHistory(String thingCode, int itemNum, int historyNum, HistoryDto target) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
        return dataAdapter.updateHistory(univ.getCode(), dept.getCode(), thingCode, itemNum, historyNum, target);
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
    
    private void checkIfRequesterHasPermissionToHistory(HistoryDto history) throws ForbiddenException {
        if(doesRequesterHaveStaffPermission()) {
            return;
        } else if(doesRequesterHaveUserPermission() && requester.getStudentId().equals(history.getUserStudentId())) {
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
    
    
    private void checkIfStatusOfHistoryIsReserved(HistoryDto history) throws MethodNotAllowedException {
        if(history.getStatus().equals("RESERVED")) {
           return;
        } else {
             throw new MethodNotAllowedException("'예약됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfStatusOfHistoryIsUsingOrDelayed(HistoryDto history) throws MethodNotAllowedException {
        if(history.getStatus().equals("USING") && !history.getStatus().equals("DELAYED")) {
            return; 
        } else {
            throw new MethodNotAllowedException("'사용중' 또는 '연체됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfStatusOfHistoryIsLost(HistoryDto history) throws MethodNotAllowedException {
        if(history.getStatus().equals("LOST")) {
            return;
        } else {
            throw new MethodNotAllowedException("'분실됨'기록이 아닙니다.");
        }
    }
    
    private void checkIfUserCanReserveThing(String userStudentId, String thingCode) throws InternalServerErrorException, MethodNotAllowedException {
        List<HistoryDto> historyListByUser = dataAdapter.findAllHistoriesByUnivCodeAndDeptCodeAndUserId(univ.getCode(), dept.getCode(), userStudentId);    
       
        int currentHistoryCount = 0;
        for(int i = 0; i < historyListByUser.size(); i++) {
            HistoryDto tmp = historyListByUser.get(i);
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentHistoryCount++;
                if(tmp.getThingCode().equalsIgnoreCase(thingCode)) {
                    throw new MethodNotAllowedException("빌리려고 하는 물품종류에 대한 열린 기록이 있습니다.");
                }
            }
        }
        if(currentHistoryCount >= 3) {
            throw new MethodNotAllowedException("사용자가 이미 3개의 물품을 사용 또는 예약하였습니다.");
        }
    }
    
    private ResponseEntity<Response> createGetResponseEntity(HistoryDto output) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createResponse(output));
    }
    
    private ResponseEntity<Response> createPostResponseEntity(String location, HistoryDto output) throws InternalServerErrorException, NotFoundException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(output));
    }
    
    private ResponseEntity<ListResponse> createGetListResponseEntity(List<HistoryDto> output) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createListResponse(output));
    }
    
    private Response createResponse(HistoryDto historyDto) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        HistoryJsonBody historyJsonBody = jsonBodyProjector.toHistoryJsonBody(historyDto);
        return new Response(univJsonBody, deptJsonBody, historyJsonBody);
    }
    
    private ListResponse createListResponse(List<HistoryDto> historyDtoList) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        List<HistoryJsonBody> historyJsonBodyList = new ArrayList<>();
        for(int i = 0; i < historyDtoList.size(); i++) {
            historyJsonBodyList.add(jsonBodyProjector.toHistoryJsonBody(historyDtoList.get(i)));
        }
        return new ListResponse(univJsonBody, deptJsonBody, historyJsonBodyList);
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
        HistoryJsonBody history;

        public Response(UniversityJsonBody university, DepartmentJsonBody department, HistoryJsonBody history) {
            this.university = university;
            this.department = department;
            this.history = history;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }
        
        public HistoryJsonBody getHistory() {
            return history;
        }
    }
    
    public class ListResponse {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        List<HistoryJsonBody> histories;

        public ListResponse(UniversityJsonBody university, DepartmentJsonBody department, List<HistoryJsonBody> histories) {
            this.university = university;
            this.department = department;
            this.histories = histories;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }

        public List<HistoryJsonBody> getHistories() {
            return histories;
        }
    }
}