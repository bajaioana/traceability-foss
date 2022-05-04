import { Injectable } from '@angular/core';
import { isEmpty, random } from 'lodash-es';
import { DateTime } from 'luxon';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from '../../core/api/api.service';
import { Tiles } from '../model/tiles.model';

/**
 *
 *
 * @export
 * @class SharedService
 */
@Injectable({
  providedIn: 'root',
})
export class SharedService {
  constructor(private apiService: ApiService) {}

  public setTodayDate(): string {
    return DateTime.local().toISODate();
  }

  public getPastDays(amountOfDays: number): string {
    return DateTime.local()
      .minus({ days: amountOfDays })
      .toISODate();
  }

  public formatDate(date: Date | string): string {
    return typeof date === 'string'
      ? DateTime.fromISO(date).toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS)
      : date.toLocaleString();
  }

  public timestampToDate(timestamp: number): DateTime {
    return DateTime.fromMillis(timestamp);
  }

  public dateToTimestamp(): number {
    return DateTime.local().toMillis();
  }

  public timestampToDateString(timestamp: string): string {
    return new Date(+timestamp * 1000).toJSON();
  }

  public firstLetterToUpperCase(word: string): string {
    return word.charAt(0).toUpperCase() + word.slice(1);
  }

  public getTiles(): Observable<Tiles> {
    return this.apiService
      .get<{
        data: Tiles;
        status: number;
      }>('/tiles')
      .pipe(map((tiles: { data: Tiles; status: number }) => tiles.data));
  }

  public getMspids(): Observable<string[]> {
    return this.apiService.get<{ data: string[] }>('/get-mspids').pipe(map((mspid: { data: string[] }) => mspid.data));
  }

  public getAllOrganizations(): Observable<string[]> {
    return this.apiService.get('/all').pipe(map((orgs: { data: string[] }) => orgs.data));
  }

  public isEmpty<T>(object: T): boolean {
    return isEmpty(object);
  }

  public generateRandomID(): string {
    const uint32 = window.crypto.getRandomValues(new Uint32Array(1))[0];
    return uint32.toString(16);
  }

  public generateRandomColor(): string {
    const colorPalette = ['#6610f2', '#e83e8c', '#fe6702', '#20c997', '#03a9f4'];
    const randomColors = Math.floor(random(colorPalette.length));
    return colorPalette[randomColors];
  }
}
