import { CommonModule } from '@angular/common';
import { Component, ContentChild, input, Input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'ui-auth-card',
  imports: [CommonModule, MatCardModule],
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.css',
})
export class AuthCard {
  // Traditioal way of implementing inputs
  @Input({required: true}) title: string = '';
  @Input() subTitle: string = '';


  // Modern way with signals
  titleNew = input<string>('');
  subTitleNew = input<string>('');

  // singals driven required input
  titleNewRequired = input.required<string>();
  

  // Output with modern signals
  onClickTitle = output<string>();



  @ContentChild(ReactiveFormsModule) form! : ReactiveFormsModule;
}
