import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';


export interface UserProfileForBS {
  name: string,
  isLoggedIn: boolean
}
@Injectable({
  providedIn: 'root',
})
export class UserStoreServiceWithBS {

  // Hot Observables
  // initialise the behavioral subject with type UserProfile
  private userSubject = new BehaviorSubject<UserProfileForBS>({name: 'Guest', isLoggedIn: false});


  // exposable user subject for subscription to receive the emited object
  user$ = this.userSubject.asObservable();

  // action function to update the subject with new values

  login(name: string) {
    this.userSubject.next({name, isLoggedIn: true});
  }

  logout() {
    this.userSubject.next({name: '%Guest', isLoggedIn: false});
  }
}
