import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

interface User {
  id: number,
  name: string
}
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private httpClient = inject(HttpClient);
  private apiUrl = "http://example.com"

  // Hot Observers example
  // exampke for Get API call
  getUser(): Observable<User> {
    let userId = 10;
    const params = new HttpParams().set('userId', userId);
    const headers = new HttpHeaders({'Authorisation': 'Bearer token232'});
    return this.httpClient.get<User>(this.apiUrl, {params, headers});
  }

  // put API call example
  postUser(): Observable<User> {
    let userId = 10;
    const params = new HttpParams().set('userId', userId);
    const headers = new HttpHeaders({'Authorisation': 'Bearer token232'});
    const userObject: User = {
      id: 10,
      name: 'manohar'
    }
    return this.httpClient.post<User>(this.apiUrl, userObject, {params, headers});
  }

  // PUT Request
  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.httpClient.put<User>(`${this.apiUrl}/${id}`, user);
  }

  // DELETE Request
  deleteUser(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/${id}`);
  }

  // an async Promise function to fetch user data immediately
  async getUserWithPromise(userID: string): Promise<User> {
    // fetch immidiately with fetch function
    const res = await fetch(this.apiUrl+''+userID);
    return res.json();
  }
}
