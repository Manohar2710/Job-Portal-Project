import { JsonPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  AbstractControl, AsyncValidatorFn, FormArray,
  FormBuilder, FormControl, ReactiveFormsModule, ValidationErrors, Validators, FormGroup
} from '@angular/forms';
import { Observable, of, delay, map } from 'rxjs';


// 1. Interface for type safety
interface ExperienceForm {
  company: FormControl<string>;
  role: FormControl<string>;
}


export function noSpaceValidator(control: AbstractControl): ValidationErrors | null {
  if (control.value && (control.value as string)?.indexOf(' ') >= 0) {
    return { noSpace: true };
  }
  return null;
}

// Custom Async Validator: Simulates checking username availability via HTTP/Observable
export function uniqueUsernameValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) {
      return of(null);
    }
    const takenUsernames = ['admin', 'john_doe', 'superuser'];

    // Simulates an API call with a 1-second delay
    return of(takenUsernames.includes(control.value.toLowerCase())).pipe(
      delay(1000),
      map(isTaken => (isTaken ? { usernameTaken: true } : null))
    );
  };
}


@Component({
  selector: 'ui-reative-form-sample',
  imports: [ReactiveFormsModule, JsonPipe],
  templateUrl: './reative-form-sample.html',
  styleUrl: './reative-form-sample.css',
})
export class ReativeFormSample {

  // build reactive form with username(nested form group), address and skills(form array)
  //  nonNullable because when we reset the form default value will be a empty string
  private fb = inject(FormBuilder).nonNullable;

  userForm = this.fb.group({
    // initailValue, syncValidators, AsyncValidators 
    userName: ['', [Validators.required, Validators.minLength(3),
      noSpaceValidator],
      [uniqueUsernameValidator()]
    ],
    role: ['user'], // Controls dependent validation
    taxId: [''],    // Dynamically updated based on role
    // nested form
    address: this.fb.group({
      city: ['Bangalore', [Validators.required]],
      zipCode: ['560097', [Validators.required]]
    }),

    // form array
    skills: this.fb.array([
      this.fb.control('Angular', [Validators.required]),
      this.fb.control('TypeScript', [Validators.required])
    ]),

    // Initialize FormArray with an array of FormGroup controls
    experiences: this.fb.array<FormGroup<ExperienceForm>>([
      this.createExperienceGroup('Angular Inc.', 'Frontend Developer')
    ])
  })

  // independent form control
  acceptTermsAndConditions = this.fb.control(false, [Validators.required]);

  // get skills from form control
  get getSkills(): FormArray<FormControl<string>> {
    return this.userForm.controls.skills;
  }

  ngOnInit() {
    // ------------------------------------------------------------------
    // 1. DEPENDENT FIELD LOGIC via valueChanges
    // Dynamically add/remove validators based on the 'role' control
    // ------------------------------------------------------------------
    const roleSub = this.userForm.controls.role.valueChanges.subscribe(selectedRole => {
      const taxIdControl = this.userForm.controls.taxId;

      if (selectedRole === 'company') {
        // Set validator dynamically for business accounts
        taxIdControl.setValidators([Validators.required]);
      } else {
        // Clear validators and reset value for standard users
        taxIdControl.clearValidators();
        taxIdControl.setValue('');
      }

      // Re-evaluate validation status of the modified control
      taxIdControl.updateValueAndValidity();
    });
  }


  // add new skills
  addSkill() {
    this.getSkills.push(this.fb.control('', [Validators.required]));
  }

  removeSkill(index: number) {
    this.getSkills.removeAt(index);
  }

  onSubmit() {
    if (this.userForm.valid && this.acceptTermsAndConditions.valid) {
      // get form value using the getRaValue
      const formData = this.userForm.getRawValue()
      console.log("form raw data ", formData)
    } else {
      this.userForm.markAllAsTouched();
    }
  }

  // Helper factory method for FormArray items
  private createExperienceGroup(company = '', role = ''): FormGroup<ExperienceForm> {
    return this.fb.group<ExperienceForm>({
      company: this.fb.control(company, [Validators.required]),
      role: this.fb.control(role)
    });
  }

  // Typed Getter for FormArray
  get experiences(): FormArray<FormGroup<ExperienceForm>> {
    return this.userForm.controls.experiences;
  }

  // 1. push(): Add a new FormGroup to array
  addExperience() {
    this.experiences.push(this.createExperienceGroup());
  }

  // 2. removeAt(): Remove control at specified index
  removeExperience(index: number) {
    if (this.experiences.length > 1) {
      this.experiences.removeAt(index);
    }
  }

  // 3. at(): Safely access a specific item at index in template/code
  getExpControl(index: number, controlName: 'company' | 'role'): FormControl<string> {
    const group = this.experiences.at(index);
    return group.controls[controlName];
  }
} 
