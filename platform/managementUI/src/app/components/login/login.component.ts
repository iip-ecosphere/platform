import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { UserService } from 'src/app/services/user.service';
import { Utils } from 'src/app/services/utils.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent extends Utils {

  loginForm: FormGroup;
  loginError = false;

  constructor(private fb: FormBuilder, private router: Router, private user: UserService, private api: ApiService) {
    super();
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  /**
   * Called from HTML to indicate that a login is happening.
   */
  async login() {
    const { username, password } = this.loginForm.value;
    this.user.setup(username, password); // TODO extend to tokens
    return await this.api.isAccessible().then(
        acc => {
            if (acc) {
                console.log('Login successful');
                this.loginError = false;
                alert('Login successful!');
                this.router.navigate(['/resources']);
            } else {
                console.log('Invalid login credentials');
                this.user.clear();
                this.loginError = true;
            }
        }).catch(err => {
            console.log('Invalid login credentials');
            this.user.clear();
            this.loginError = true;
        });
  }

}
