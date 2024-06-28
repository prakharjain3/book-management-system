package com.sismics.books.core.dao.jpa.mapper;

import com.sismics.books.core.dao.jpa.dto.UserAppDto;
import com.sismics.books.core.model.jpa.UserApp;

/**
 * This class is responsible for mapping between the UserApp entity and the UserAppDto data transfer object.
 */
public class UserAppMapper {


    /**
     * Maps a UserApp entity to a UserAppDto, excluding the userId field.
     * @param userApp The UserApp entity to map
     * @return The mapped UserAppDto
     */
    private UserAppDto mapToDtoWithoutUserId(UserApp userApp) {
        UserAppDto userAppDto = new UserAppDto();
        userAppDto.setAppId(userApp.getAppId());
        userAppDto.setId(userApp.getId());
        userAppDto.setAccessToken(userApp.getAccessToken());
        userAppDto.setUsername(userApp.getUsername());
        userAppDto.setSharing(userApp.isSharing());
        return userAppDto;
    }

    /**
     * Maps a UserApp entity to a UserAppDto, setting the userId field to a specific value.
     * @param userApp The UserApp entity to map
     * @param userId The userId to set in the UserAppDto
     * @return The mapped UserAppDto
     */
    public UserAppDto mapToDtoByUserId(UserApp userApp, String userId) {
        UserAppDto userAppDto = mapToDtoWithoutUserId(userApp);
        userAppDto.setUserId(userId);
        return userAppDto;

    }

    /**
     * Maps a UserApp entity to a UserAppDto, setting the userId field to the userId of the UserApp entity.
     * @param userApp The UserApp entity to map
     * @return The mapped UserAppDto
     */
    public UserAppDto mapToDtoByAppId(UserApp userApp) {
        UserAppDto userAppDto = mapToDtoWithoutUserId(userApp);
        userAppDto.setUserId(userApp.getUserId());
        return userAppDto;

    }

    /**
     * Creates a UserApp entity from an array of objects.
     * @param obj The array of objects to create the UserApp entity from
     * @return The created UserApp entity
     */
    private UserApp createUserAppFromObjectArray1(Object[] obj) {

        UserApp userApp = new UserApp();
        userApp.setAppId((String) obj[0]);
        userApp.setId((String) obj[1]);
        userApp.setAccessToken((String) obj[2]);
        userApp.setUsername((String) obj[3]);
        Boolean sharing = (Boolean) obj[4];
        userApp.setSharing(sharing != null ? sharing : false);
        return userApp;
    }

    /**
     * Maps an array of objects to a UserAppDto, setting the userId field to a specific value.
     * @param obj The array of objects to map
     * @param userId The userId to set in the UserAppDto
     * @return The mapped UserAppDto
     */
    public UserAppDto mapByUserId(Object[] obj, String userId) {
        UserApp userApp = createUserAppFromObjectArray1(obj);
        return mapToDtoByUserId(userApp, userId);
    }

    /**
     * Creates a UserApp entity from an array of objects.
     * @param obj The array of objects to create the UserApp entity from
     * @return The created UserApp entity
     */
    private UserApp createUserAppFromObjectArray2(Object[] obj) {
        UserApp userApp = new UserApp();
        userApp.setId((String) obj[0]);
        userApp.setAppId((String) obj[1]);
        userApp.setAccessToken((String) obj[2]);
        userApp.setUsername((String) obj[3]);
        Boolean sharing = (Boolean) obj[4];
        userApp.setSharing(sharing != null ? sharing : false);
        return userApp;
    }

    /**
     * Maps an array of objects to a UserAppDto, setting the userId field to a specific value.
     * @param obj The array of objects to map
     * @param userId The userId to set in the UserAppDto
     * @return The mapped UserAppDto
     */
    public UserAppDto mapConnectedByUserId(Object[] obj, String userId) {
        UserApp userApp = createUserAppFromObjectArray2(obj);
        return mapToDtoByUserId(userApp, userId);
    }

    /**
     * Creates a UserApp entity from an array of objects.
     * @param obj The array of objects to create the UserApp entity from
     * @return The created UserApp entity
     */
    private UserApp createUserAppFromObjectArray3(Object[] obj) {
        UserApp userApp = new UserApp();
        userApp.setId((String) obj[0]);
        userApp.setUserId((String) obj[1]);
        userApp.setAppId((String) obj[2]);
        userApp.setAccessToken((String) obj[2]);
        userApp.setUsername((String) obj[3]);
        Boolean sharing = (Boolean) obj[4];
        userApp.setSharing(sharing != null ? sharing : false);
        return userApp;
    }

    /**
     * Maps an array of objects to a UserAppDto, setting the userId field to the userId of the UserApp entity.
     * @param obj The array of objects to map
     * @return The mapped UserAppDto
     */
    public UserAppDto mapByAppId(Object[] obj) {
        UserApp userApp = createUserAppFromObjectArray3(obj);
        return mapToDtoByAppId(userApp);
    }
    
}
