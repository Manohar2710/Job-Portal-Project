import { Component } from '@angular/core';
import { AuthControllerService, LoginRequest } from '../../../../../../libs/job-portal-api';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent {

    constructor(private authApi: AuthControllerService){
      
    }
    ngOnInit() {
      console.log("App ngOninit")
      let loginRequest : LoginRequest = {
        email: "test@gmail.com",
        password: "12345"
      }
      this.authApi.login(loginRequest).subscribe( (res) =>
        console.log("response "+ res)
      );
    }
}
