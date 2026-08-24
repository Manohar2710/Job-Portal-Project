import { Component, computed, effect, inject, signal, WritableSignal } from '@angular/core';
import { AuthenticationService, LoginRequest } from '../../../../../../libs/job-portal-api';
import { User, UserService, UserStoreService, UserStoreServiceWithBS } from 'auth-feature';
import { CommonModule } from '@angular/common';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, Observable, switchMap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [CommonModule],
})
export class HomeComponent {
  private http = inject(HttpClient);

  // To Observables example
  searchQuery = signal('');

  userList$! : Observable<User[]>;

  // Inject User service
  private userService = inject(UserService);

  // fetchs user data and updates the immutable userData with data of type signal
  userData = toSignal(
      this.userService.getUser(),
      {initialValue: {} as User}
    );

  // Singals with Computing and effect Example
  name = signal('Manohar');
  age = signal(29);

  // updates computedStatement everytime name or age gets updated
  computedStatement = computed(() => `${this.name()} is of age ${this.age()}`);

  // inject userstate signal example
  userStateService = inject(UserStoreService);

  // inject user state behavioral subject example
  userStateWithBS = inject(UserStoreServiceWithBS);

  // Effect should be used inside a constructor and gets invoked when the dependency changes
  constructor(private authApi: AuthenticationService) {
    effect(() => {
      console.log(`Updated Name : ${this.name()} and age : ${this.age()} `);
    });
  }

  ngOnInit() {
    console.log('App ngOninit');
    let loginRequest: LoginRequest = {
      email: 'testemail1@gmail.com',
      password: 'test_password',
    };
    this.authApi.login(loginRequest).subscribe((res) => console.log('response ' + res));

    // update signal values at run time after 1 sec
    setTimeout(() => {
      this.name.set('Manohar B');
      this.age.set(30);
    }, 1000);

    // to assgin a observable from a signal input
    this.userList$ = toObservable(this.searchQuery).pipe(
      debounceTime(300),
      switchMap( query => this.http.get<User[]>(`api/users?q=${query}`))
    )
  }

}
