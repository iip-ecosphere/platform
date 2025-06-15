import { Injectable } from '@angular/core';
import { DataUtils, UtilsService } from './utils.service';
import { HttpHeaders, HttpRequest } from '@angular/common/http';

/**
 * Represents an user service storing the authentication information of a user, including plain old and token based approaches.
 */
@Injectable({
    providedIn: 'root'
})
export class UserService extends UtilsService {

    user: string = "";
    password: string = "";
    // add token, use then as alternative in inject

    /**
     * Sets up the user information, e.g., on login.
     * @param user 
     * @param password 
     */
    public setup(user: string, password: string) {
        this.user = user;
        this.password = password;
    }

    /**
     * Cleares the user information, e.g., on logout. 
     */
    public clear() {
        this.user = "";
        this.password = "";
    }

    /**
     * Injects the user information into an HTTP header.
     * 
     * @param headers the headers structure to inject into
     * @returns headers, potentially a modified/other instance
     */
    public injectTo(headers: HttpHeaders): HttpHeaders {
        if (this.user) {
            var token = this.user + ":" + this.password;
            headers = headers.set("Authorization", DataUtils.stringToBase64(token));
        } // TODO handle other token encoding, also oauth
        return headers;
    }

}
  