import { computed, Injectable, signal } from '@angular/core';


export interface UserProfile {
  name: string,
  isLoggedIn: boolean
}

@Injectable({
  providedIn: 'root', // provides single instance accross the application
})
export class UserStoreService {

 //Initialize user data with type signal
 private userSignal = signal<UserProfile>({name: 'Guest', isLoggedIn: false});

 // expose user with read only state for consuming in components
 readonly user = this.userSignal.asReadonly();

 // compute and update isLoggedIn flag for user data change
 readonly isLoggerIn = computed(() => this.userSignal().isLoggedIn)

 // actions to mutate user state
 login(name: string) {
  this.userSignal.set({name, isLoggedIn: true});
 }

 logout(){
  this.userSignal.set({name: 'Guest', isLoggedIn: false});
 }


}
