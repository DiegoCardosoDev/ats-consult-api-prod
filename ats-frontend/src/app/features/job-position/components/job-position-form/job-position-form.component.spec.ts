import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JobPositionFormComponent } from './job-position-form.component';

describe('JobPositionFormComponent', () => {
  let component: JobPositionFormComponent;
  let fixture: ComponentFixture<JobPositionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobPositionFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(JobPositionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
