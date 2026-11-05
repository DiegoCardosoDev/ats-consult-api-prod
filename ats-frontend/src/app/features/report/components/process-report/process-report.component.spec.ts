import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ProcessReportComponent } from './process-report.component';
import { ProcessReportItem } from '../../models/process-report.model';

describe('ProcessReportComponent', () => {
  let component: ProcessReportComponent;
  let fixture: ComponentFixture<ProcessReportComponent>;

  const item: ProcessReportItem = {
    processId: 10,
    client: { id: 1, name: 'Cliente', code: 'CLI' },
    jobPosition: { id: 2, title: 'Vaga' },
    processStatus: 'OPEN',
    openedAt: '2026-01-01',
    totalApplications: 3,
    inTriage: 2,
    rejected: 1,
    approvedByClient: 0,
    hired: 0,
    declined: 0,
    standBy: 0,
    statusCounts: { TRIAGEM: 2, REPROVADO: 1 }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProcessReportComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProcessReportComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return per-process status counts with fallback to zero', () => {
    expect(component.applicationCount(item, 'TRIAGEM')).toBe(2);
    expect(component.applicationCount(item, 'REPROVADO')).toBe(1);
    expect(component.applicationCount(item, 'DECLINOU')).toBe(0);
    expect(component.applicationCount(item, 'STAND_BY')).toBe(0);
  });

  it('should toggle the expanded detail of a process', () => {
    expect(component.isExpanded(item.processId)).toBeFalse();

    component.toggleDetails(item.processId);
    expect(component.isExpanded(item.processId)).toBeTrue();

    component.toggleDetails(item.processId);
    expect(component.isExpanded(item.processId)).toBeFalse();
  });
});
