import { Component } from '@angular/core';
import { AuthenticationService, LoginRequest } from '../../../../../../libs/job-portal-api';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent {

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
