import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ProcessReportDashboardComponent } from './process-report-dashboard.component';
import { ProcessDashboard } from '../../models/process-dashboard.model';

describe('ProcessReportDashboardComponent', () => {
  let component: ProcessReportDashboardComponent;
  let fixture: ComponentFixture<ProcessReportDashboardComponent>;

  const dashboard: ProcessDashboard = {
    processId: 10,
    client: { id: 1, name: 'Cliente', code: 'CLI' },
    jobPosition: { id: 2, title: 'Vaga' },
    status: 'OPEN',
    openedAt: '2026-11-01',
    daysOpen: 3,
    totalApplications: 4,
    activeApplications: 2,
    inTriage: 2,
    approvedByClient: 1,
    hired: 1,
    rejected: 1,
    declined: 0,
    standBy: 0,
    averageAdherence: 80,
    highAdherenceCount: 2,
    conversionRate: 25,
    statusCounts: { TRIAGEM: 2, APROVADO_CLIENTE: 1, CONTRATADO: 1, REPROVADO: 0 },
    timeline: [
      { date: '2026-11-01', total: 1 },
      { date: '2026-11-02', total: 3 }
    ],
    topCandidates: [
      {
        applicationId: 1,
        candidateId: 9,
        candidateName: 'Ana Silva',
        status: 'TRIAGEM',
        adherence: 90,
        appliedAt: '2026-11-01T10:00:00Z'
      }
    ]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProcessReportDashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProcessReportDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should compute phase stats and a donut gradient from status counts', () => {
    component.dashboard = dashboard;

    const triage = component.phaseStats.find((phase) => phase.status === 'TRIAGEM');
    const standBy = component.phaseStats.find((phase) => phase.status === 'STAND_BY');

    expect(component.phaseStats.length).toBe(9);
    expect(triage?.total).toBe(2);
    expect(triage?.percentage).toBe(50);
    expect(standBy?.total).toBe(0);
    expect(component.donutGradient).toContain('conic-gradient');
    expect(component.donutGradient).not.toContain('#e2e8f0 100%');
  });

  it('should build timeline geometry and candidate helpers', () => {
    component.dashboard = dashboard;

    expect(component.timelinePoints.length).toBe(2);
    expect(component.timelineAreaPath.startsWith('M ')).toBeTrue();

    expect(component.initials('Ana Silva')).toBe('AS');
    expect(component.initials(null)).toBe('?');
    expect(component.phaseFor('CONTRATADO').label).toBe('Contratado');
    expect(component.statusLabel('OPEN')).toBe('Aberto');
  });

  it('should fall back to an empty donut when there are no applications', () => {
    component.dashboard = { ...dashboard, totalApplications: 0, statusCounts: {} };

    expect(component.donutGradient).toBe('conic-gradient(#e2e8f0 0% 100%)');
  });
});
