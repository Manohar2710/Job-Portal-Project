import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { AuthCard } from 'shared-ui';

@Component({
  selector: 'app-login-with-template-form',
  imports: [FormsModule, AuthCard, MatInputModule],
  templateUrl: './login-with-template-form.html',
  styleUrl: './login-with-template-form.scss',
})
export class LoginWithTemplateForm {
  userModel = {
    user: {
      username: '',
      password:''
    }
  }

  onSubmit() {
    // submit credentials to BE
    console.log(this.userModel)
  }
}
