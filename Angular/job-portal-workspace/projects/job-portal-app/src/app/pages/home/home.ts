import { Component, inject } from '@angular/core';
import { AuthenticationService, LoginRequest } from '../../../../../../libs/job-portal-api';
import { UserStoreService, UserStoreServiceWithBS } from 'auth-feature';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [CommonModule]
})
export class HomeComponent {

    // inject userstate signal example
    userStateService = inject(UserStoreService);

    // inject user state behavioral subject example
    userStateWithBS = inject(UserStoreServiceWithBS)

    constructor(private authApi: AuthenticationService){
      
    }
    ngOnInit() {
      console.log("App ngOninit")
      let loginRequest : LoginRequest = {
        email: "testemail1@gmail.com",
        password: "test_password"
      }
      this.authApi.login(loginRequest).subscribe( (res) =>
        console.log("response "+ res)
      );
    }
}
