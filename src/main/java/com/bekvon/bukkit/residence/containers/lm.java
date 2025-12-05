package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.TitleMessages.CMITitleMessage;

public enum lm {
    Invalid_Player("&cInvalid player name..."),
    Invalid_PlayerOffline("&cPlayer is offline"),
    Invalid_World("&cInvalid world..."),
    Invalid_Residence("&cInvalid Residence..."),
    Invalid_Subzone("&cInvalid Subzone..."),
    Invalid_Direction("&cInvalid Direction..."),
    Invalid_Amount("&cInvalid Amount..."),
    Invalid_Cost("&cInvalid Cost..."),
    Invalid_Days("&cInvalid number of days..."),
    Invalid_Material("&cInvalid Material..."),
    Invalid_Boolean("&cInvalid value, must be &6true(t) &cor &6false(f)"),
    Invalid_Area("&cInvalid Area..."),
    Invalid_Group("&cInvalid Group..."),
    Invalid_Location("&cInvalid Location..."),
    Invalid_MessageType("&cMessage type must be enter or remove."),
    Invalid_Flag("&cInvalid Flag..."),
    Invalid_FlagType_Fail("&cInvalid Flag... This flag can only be used on %1"),
    Invalid_FlagType_Player("Player"),
    Invalid_FlagType_Residence("Residence"),
    Invalid_FlagState("&cInvalid flag state, must be &6true(t)&c, &6false(f)&c, or &6remove(r)"),
    Invalid_List("&eUnknown list type, must be &6blacklist &eor &6ignorelist."),
    Invalid_Page("&eInvalid Page..."),
    Invalid_Help("&cInvalid Help Page..."),
    Invalid_NameCharacters("&cName contained unallowed characters..."),
    Invalid_NameLegth("&cName length too long, max %1 characters"),
    Invalid_PortalDestination("&cPortal destination is in restricted zone. Portal creation canceled. &7Find new location"),

    Invalid_FromConsole("&cYou can only use this in the console!"),
    Invalid_Ingame("&cYou can only use this in game!"),

    Area_Exists("&cArea name already exists."),
    Area_Create("&eResidence Area created, ID &6%1"),
    Area_DiffWorld("&cArea is in a different world from residence."),
    Area_Collision("&cArea collides with residence &6%1"),
    Area_TooClose("&cToo close to another residence. You need atleast &e%1 &cblock gap."),
    Area_SubzoneCollision("&cArea collides with subzone &6%1"),
    Area_NonExist("&cNo such area exists."),
    Area_InvalidName("&cInvalid Area Name..."),
//    Area_ToSmallTotal("&cSelected area smaller than allowed minimal (&6%1&c)"),
    Area_ToSmallX("&cYour &6X &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more."),
    Area_ToSmallY("&cYour selection height (&6%1&c) is too small. &eAllowed &6%2 &eand more."),
    Area_ToSmallZ("&cYour &6Z &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more."),
    Area_ToBigX("&cYour &6X &cselection length (&6%1&c) is too big. &eAllowed &6%2 &eand less."),
    Area_ToBigY("&cYour selection height (&6%1&c) is too big. &eAllowed &6%2 &eand less."),
    Area_ToBigZ("&cYour &6Z &cselection length (&6%1&c) is too big. &eAllowed &6%2 &eand less."),
    Area_Rename("&eRenamed area &6%1 &eto &6%2"),
    Area_Remove("&eRemoved area &6%1..."),
    Area_Name("&eName: &2%1"),

    Area_ListAll("&a{&eID:&c%1 &eP1:&c(%2,%3,%4) &eP2:&c(%5,%6,%7) &e(Size:&c%8&e)&a}"),

    Area_RemoveLast("&cCannot remove the last area in a residence."),
    Area_NotWithinParent("&cArea is not within parent area."),
    Area_Update("&eArea Updated..."),
    Area_MaxPhysical("&eYou've reached the max physical areas allowed for your residence."),
    Area_SizeLimit("&eArea size is not within your allowed limits."),
    Area_HighLimit("&cYou cannot protect this high up, your limit is &6%1"),
    Area_LowLimit("&cYou cannot protect this deep, your limit is &6%1"),

    Area_WeirdShape("&3Residence is out of regular shape. &6%1 &3side is &6%2 &3times bigger than &6%3 &3side"),

    Select_Points("&eSelect two points first before using this command!"),
    Select_Overlap("&cSelected points overlap with &6%1 &cregion!"),
    Select_WorldGuardOverlap("&cSelected points overlap with &6%1 &cWorldGuard region!"),
    Select_KingdomsOverlap("&cSelected points overlap with &6%1 &cKingdoms land!"),
    Select_Success("&eSelection Successful!"),
    Select_Fail("&cInvalid select command..."),
    Select_Bedrock("&eSelection expanded to your lowest allowed limit."),
    Select_Sky("&eSelection expanded to your highest allowed limit."),
    Select_Area("&eSelected area &6%1 &eof residence &6%2"),
    Select_Tool("&e- Selection Tool: &6%1"),
    Select_PrimaryPoint("&ePlaced &6Primary &eSelection Point %1"),
    Select_SecondaryPoint("&ePlaced &6Secondary &eSelection Point %1"),
    Select_Primary("&ePrimary selection: &6%1"),
    Select_Secondary("&eSecondary selection: &6%1"),
    Select_TooHigh("&cWarning, selection went above top of map, limiting."),
    Select_TooLow("&cWarning, selection went below bottom of map, limiting."),
    Select_TotalSize("&eSelection total size: &6%1"),
    Select_AutoEnabled("&eAuto selection mode turned &6ON&e. To disable it write &6/res select auto"),
    Select_AutoDisabled("&eAuto selection mode turned &6OFF&e. To enable it again write &6/res select auto"),
    Select_Disabled("&cYou don't have access to selections commands"),

    Sign_Updated("&6%1 &esigns updated!"),
    Sign_TopLine("[market]"),
    Sign_DateFormat("YY/MM/dd HH:mm"),
    Sign_ForRentTopLine("&0For Rent"),
    Sign_ForRentPriceLine("&0%1&f/&0%2&f/&0%3"),
    Sign_ForRentResName("&0%1"),
    Sign_ForRentBottomLine("&9Available"),
    Sign_RentedAutorenewTrue("&2%1"),
    Sign_RentedAutorenewFalse("&c%1"),
    Sign_RentedTopLine("%1"),
    Sign_RentedPriceLine("&0%1&f/&0%2&f/&0%3"),
    Sign_RentedResName("&0%1"),
    Sign_RentedBottomLine("&1%1"),
    Sign_ForSaleTopLine("&0For Sale"),
    Sign_ForSalePriceLine("&0%1"),
    Sign_ForSaleResName("&0%1"),
    Sign_ForSaleBottom("&0%1m\u00B3"),
    Sign_LookAt("&cYou are not looking at sign"),
    Sign_TooMany("&cToo many signs for this residence"),
    Sign_ResName("&0%1"),
    Sign_Owner("&0%1"),

    Raid_NotEnabled("&cRaid feature is not enabled!"),
    Raid_NotIn("&cYou are not in the raid!"),
    Raid_CantLeave("&cYou cant leave (%1) your own residence raid!"),
    Raid_CantKick("&cCant kick (%1) residence owner!"),
    Raid_Kicked("&eKicked &7%1 &efrom &7%2 &eresidence raid!"),
    Raid_StartsIn("&7Raid starts in: [autoTimeLeft] &2%1D &4%2A"),
    Raid_EndsIn("&cRaid ends in: [autoTimeLeft] &2%1D &4%2A"),

    Raid_Ended("&7Raid on &4%1 &7ended!"),
    Raid_cantDo("&cCan't do this during raid!"),
    Raid_left("&7You have left &4%1 &7raid"),
    Raid_noFlagChange("&cCan't change flags during raid"),
    Raid_noRemoval("&cCan't remove residence during raid"),
    Raid_immune("&eImmune for next %1"),
    Raid_notImmune("&eNo longer immune"),
    Raid_cantStart("&cCan't start raid"),
    Raid_cantJoin("&cCan't join raid"),

    Raid_notInRaid("&ePlayer isn't in raid"),

    Raid_attack_Joined("&7Joined &2%1 &7raid!"),
    Raid_attack_Started("&7Raid started!"),
    Raid_attack_cooldown("&cToo soon for another raid on this residence! Wait %1"),
    Raid_attack_immune("&cThis residence is immune to raids! Wait %1"),
    Raid_attack_playerImmune("&cResidence owner is immune to raids! Wait %1"),
    Raid_attack_isOffline("&cCan't raid while owner is offline!"),
    Raid_attack_noSubzones("&cCan't raid subzones!"),
    Raid_attack_noSelf("&cCan't raid your own residence!"),
    Raid_attack_alreadyInAnother("&cCan't join this raid, you are in another one already (%1)"),

    Raid_defend_Joined("&7Joined &2%1 &7defence forces!"),
    Raid_defend_Sent("&7Request to join raid defence is sent, wait for confirmation"),
    Raid_defend_Join("&7Join &6%1 &7raid defence"),
    Raid_defend_Invitation("&7Accept raid defend from &6%1"),
    Raid_defend_JoinedDef("&2%1&7 joined defence forces!"),
    Raid_defend_IsOffline("&cCan't join defend team while owner is offline!"),
    Raid_defend_noSelf("&cYou already defending this residence"),
    Raid_defend_notRaided("&cResidence is not under the raid"),
    Raid_defend_alreadyInAnother("&cCan't join this residence defence, you are in another one already (%1)"),

    Raid_status_title("&7----------- &f%1(%2) &7-----------"),
    Raid_status_immune("&eImmune to raids for next: %1"),
    Raid_status_starts("&7Raid starts in: %1"),
    Raid_status_attackers("&7Attackers: &4%1"),
    Raid_status_defenders("&7Defenders: &4%1"),
    Raid_status_ends("&7Raid ends in: %1"),
    Raid_status_canraid("&2Can be raided"),
    Raid_status_raidin("&ePossible raid in: %1"),

    Raid_stopped("&eRaid on &6%1 &ewas stopped"),

    info_clickToConfirm("&7Click to confirm"),

    server_land("Server_Land"),

    Flag_p1Color("&2"),
    Flag_p2Color("&a"),
    Flag_haveColor("&2"),
    Flag_havePrefix(""),
    Flag_lackColor("&7"),
    Flag_lackPrefix(""),
    Flag_others("&eand &2%1 &eothers"),
    Flag_Set("&eFlag (&6%1&e) set for &6%2 &eto &6%3 &estate"),
    Flag_GSet("&eFlags (&6%1&e) set for &6%2"),
    Flag_Removed("&eFlag (&6%1&e) removed for &6%2"),
    Flag_PSet("&eFlag (&6%1&e) set for &6%2 &eto &6%3 &estate for &6%4"),
    Flag_PRemoved("&eFlag (&6%1&e) removed from &6%2 &eresidence for &6%3"),
    Flag_SetFailed("&cYou don't have access to &6%1 &cflag"),
    Flag_CheckTrue("&eFlag &6%1 &eapplies to player &6%2 &efor residence &6%3&e, value = &6%4"),
    Flag_CheckFalse("&eFlag &6%1 &edoes not apply to player &6%2 &efor residence."),
    Flag_Cleared("&eFlags Cleared."),
    Flag_RemovedAll("&eAll flags removed for &6%1 &ein &6%2 &eresidence."),
    Flag_RemovedGroup("&eAll flags removed for &6%1 &egroup in &6%2 &eresidence."),
    Flag_Default("&eFlags set to default."),
    Flag_Deny("&cYou don't have &6%1 &cpermission<s> here."),
    Flag_SetDeny("&cOwner does not have access to flag &6%1"),
    Flag_ChangeDeny("&cYou cant change &6%1 &cflag state while there is &6%2 &cplayer(s) inside."),
    Flag_ChangedForOne("&eChanged &6%1 &eflags for &6%2 &eresidences"),
    Flag_ChangedFor("&eChanged &6%1 &eflags from &6%2 &eresidences checked"),
    Flag_reset("&eReset flags for &6%1 &eresidence"),
    Flag_resetAll("&eReset flags for &6%1 &eresidences"),

    Bank_NoAccess("&cYou don't have bank access."),
    Bank_Name(" &eBank: &6%1"),
    Bank_NoMoney("&cNot enough money in the bank."),
    Bank_Deposit("&eYou deposit &6%1 &einto the residence bank."),
    Bank_Withdraw("&eYou withdraw &6%1 &efrom the residence bank."),
    Bank_rentedWithdraw("&cCan't withdraw from rented residence bank."),
    Bank_full("&eResidence bank is full!"),

    Subzone_Rename("&eRenamed subzone &6%1 &eto &6%2"),
    Subzone_Remove("&eSubzone &6%1 &eremoved."),
    Subzone_Create("&eCreated Subzone &6%1"),
    Subzone_CreateFail("&cUnable to create subzone &6%1"),
    Subzone_Exists("&cSubzone &6%1 &calready exists."),
    Subzone_Collide("&cSubzone collides with subzone &6%1"),
    Subzone_MaxAmount("&cYou have reached the maximum allowed subzone amount for this residence."),
    Subzone_MaxDepth("&cYou have reached the maximum allowed subzone depth."),
    Subzone_SelectInside("&eBoth selection points must be inside the residence."),
    Subzone_CantCreate("&cYou don't have permission to create residence subzone."),
    Subzone_CantDelete("&cYou don't have permission to delete residence subzone."),
    Subzone_CantDeleteNotOwnerOfParent("&cYou are not owner of parent residence to delete this subzone."),
    Subzone_CantContract("&cYou don't have permission to contract residence subzone."),
    Subzone_CantExpand("&cYou don't have permission to expand residence subzone."),
    Subzone_DeleteConfirm("&eAre you sure you want to delete subzone &6%1&e, use &6/res confirm &eto confirm."),
    Subzone_OwnerChange("&eSubzone &6%1 &eowner changed to &6%2"),

    Residence_DontOwn("&eNothing to show"),
    Residence_Hidden(" &e(&6Hidden&e)"),
    Residence_Bought("&eYou bought residence &6%1"),
    Residence_Buy("&6%1 &ehas bought residence &6%2 &efrom you."),
    Residence_BuyTooBig("&cThis residence has areas bigger then your allowed max."),
    Residence_NotForSale("&cResidence is not for sale."),
    Residence_ForSale("&eResidence &6%1 &eis now for sale for &6%2"),
    Residence_StopSelling("&cResidence is no longer for sale."),
    Residence_TooMany("&cYou already own the max number of residences your allowed to."),
    Residence_MaxRent("&cYou already are renting the maximum number of residences your allowed to."),
    Residence_AlreadyRent("&cResidence is already for rent..."),
    Residence_NotForRent("&cResidence not for rent..."),
    Residence_NotForRentOrSell("&cResidence not for rent or sell..."),
    Residence_NotRented("&cResidence not rented."),
    Residence_Unrent("&eResidence &6%1 &ehas been unrented."),
    Residence_RemoveRentable("&eResidence &6%1 &eis no longer rentable."),
    Residence_ForRentSuccess("&eResidence &6%1 &eis now for rent for &6%2 &eevery &6%3 &edays."),
    Residence_RentSuccess("&eYou have rented Residence &6%1 &efor &6%2 &edays."),
    Residence_EndingRent("&eRent time is ending for &6%1 &eon &6%2"),
    Residence_AlreadyRented("&eResidence &6%1 &ehas currently been rented to &6%2"),
    Residence_CantAutoPay("&eResidence is not allowing auto pay, it will be set to &6false"),
    Residence_AlreadyExists("&cA residence named &6%1 &calready exists."),
    Residence_Create("&eYou have created residence &6%1&e!"),
    Residence_Rename("&eRenamed Residence &6%1 &eto &6%2"),
    Residence_Remove("&eResidence &6%1 &ehas been removed..."),
    Residence_CantRemove("&cResidence &6%1 &ccant be removed as &6%2 &csubzone is still rented by &6%3"),
    Residence_MoveDeny("&cYou don't have movement permission for Residence &6%1"),
    Residence_TeleportNoFlag("&cYou don't have teleport access for that residence."),
    Residence_FlagDeny("&cYou don't have &6%1 &cpermission for &6%2 &cresidence"),
    Residence_BaseFlagDeny("&cYou don't have &6%1 &cpermission"),
    Residence_GiveLimits("&cCannot give residence to target player, because it is outside the target players limits."),
    Residence_GiveConfirm("&7Click to confirm &6%1 &7residence transfer from &6%2 &7to &6%3"),
    Residence_Give("&eYou give residence &6%1 &eto player &6%2"),
    Residence_Received("&eYou have received residence &6%1 &efrom player &6%2"),
    Residence_ResList(" &a%1. &e%2 &e- &6%3 %4&6%5"),
    Residence_TrustedResList(" &a%1. &f%2 &e- &6%3 %4&6%5"),
    Residence_List(" &e%2 &e- &6%3"),
    Residence_Near("&eNearby residences: &7%1"),
    Residence_TeleportNear("&eTeleported to near residence."),
    Residence_SetTeleportLocation("&eTeleport Location Set..."),
    Residence_TeleportBlockedWorlds("&cCan't teleport to residence in blocked world"),
    Residence_PermissionsApply("&ePermissions applied to residence."),
    Residence_NotOwner("&cYou are not owner of this residence"),
    Residence_RemovePlayersResidences("&eRemoved all residences belonging to player &6%1"),
    Residence_NotIn("&cYou are not in a Residence."),
    Residence_PlayerNotIn("&cPlayer standing not in your Residence area."),
    Residence_Kicked("&eYou were kicked from residence"),
    Residence_CantKick("&eCan't kick this player"),
    Residence_In("&eYou are standing in Residence &6%1"),
    Residence_OwnerChange("&eResidence &6%1 &eowner changed to &6%2"),
    Residence_NonAdmin("&cYou are not a Residence admin."),
    Residence_Line("&eResidence: &6%1 "),
    Residence_RentedBy("&eRented by: &6%1"),
    Residence_MessageChange("&eMessage Set..."),
    Residence_CantDeleteResidence("&cYou don't have permission to delete residence."),
    Residence_CantExpandResidence("&cYou don't have permission to expand residence."),
    Residence_CantContractResidence("&cYou don't have permission to contract residence."),
    Residence_NoResHere("&cThere is no residence in there."),
    Residence_OwnerNoPermission("&cThe owner does not have permission for this."),
    Residence_ParentNoPermission("&cYou don't have permission to make changes to the parent zone."),
    Residence_ChatDisabled("&eResidence Chat Disabled..."),
    Residence_DeleteConfirm("&eAre you sure you want to delete residence &6%1&e, use &6/res confirm &eto confirm."),
    Residence_ChangedMain("&eChanged main residence to &6%1"),
    Residence_LwcRemoved("&eRemoved &6%1 &eLwc protections in &6%2ms"),

    Residence_Balance("&eBalance: &6%1"),

    Residence_CanBeRented("&6%1&e can be rented for &6%2 &eper &6%3 &edays. &6/res market rent"),
    Residence_CanBeBought("&6%1&e can be bought for &6%2&e. &6/res market buy"),

    Residence_IsForRent("&6(For rent)"),
    Residence_IsForSale("&6(For sale)"),
    Residence_IsRented("&6(Rented)"),

    Rent_Disabled("&cRent is disabled..."),
    Rent_DisableRenew("&eResidence &6%1 &ewill now no longer re-rent upon expire."),
    Rent_EnableRenew("&eResidence &6%1 &ewill now automatically re-rent upon expire."),
    Rent_NotByYou("&cResidence is rented not by you."),
    Rent_isForRent("&2Residence available for renting."),
    Rent_MaxRentDays("&cYou cant rent for more than &6%1 &cdays at once."),
    Rent_OneTime("&cCan't extend rent time for this residence."),
    Rent_Extended("&eRent extended for aditional &6%1 &edays for &6%2 &eresidence"),
    Rent_Expire("&eRent Expire Time: &6%1"),
    Rent_AutoPayTurnedOn("&eAutoPay is turned &2ON"),
    Rent_AutoPayTurnedOff("&eAutoPay is turned &cOFF"),
    Rent_ModifyDeny("&cCannot modify a rented residence."),
    Rent_Days("&eRent days: &6%1"),
    Rent_Rented(" &6(Rented)"),
    Rent_RentList(" &6%1&e. &6%2 &e(&6%3&e/&6%4&e/&6%5&e) - &6%6 &6%7"),

    Rent_EvictConfirm("&eWrite &6/res market confirm &eto evict renter from &6%1 &eresidence"),
    Rent_UnrentConfirm("&eWrite &6/res market confirm &eto unrent &6%1 &eresidence"),
    Rent_ReleaseConfirm("&eWrite &6/res market confirm &eto remove &6%1 &eresidence from market"),

    command_addedAllow("&eAdded new allowed command for &6%1 &eresidence"),
    command_removedAllow("&eRemoved allowed command for &6%1 &eresidence"),
    command_addedBlock("&eAdded new blocked command for &6%1 &eresidence"),
    command_removedBlock("&eRemoved blocked command for &6%1 &eresidence"),
    command_Blocked("&eBlocked commands: &6%1"),
    command_Allowed("&eAllowed commands: &6%1"),

    command_Parsed("%1"),
    command_PlacehlderList("&e%1. &6%2"),
    command_PlacehlderResult(" &eresult: &6%1"),

    Rentable_Land("&eRentable Land: &6"),
    Rentable_AllowRenewing("&eCan Renew: &6%1"),
    Rentable_StayInMarket("&eRentable stay in market: &6%1"),
    Rentable_AllowAutoPay("&eRentable allows auto pay: &6%1"),
    Rentable_DisableRenew("&6%1 &ewill no longer renew rentable status upon expire."),
    Rentable_EnableRenew("&6%1 &ewill now automatically renew rentable status upon expire."),

    Economy_LandForSale("&eLand For Sale:"),
    Economy_NotEnoughMoney("&cYou don't have enough money."),
    Economy_NotEnoughMoneyAmount("&cYou don't have enough money. (&6%1&c)"),
    Economy_MoneyCharged("&eCharged &6%1 &eto your &6%2 &eaccount."),
    Economy_MoneyAdded("&eGot &6%1 &eto your &6%2 &eaccount."),
    Economy_MoneyCredit("&eCredited &6%1 &eto your &6%2 &eaccount."),
    Economy_RentReleaseInvalid("&eResidence &6%1 &eis not rented or for rent."),
    Economy_RentSellFail("&cCannot sell a Residence if it is for rent."),
    Economy_SubzoneRentSellFail("&cCannot sell a Residence if its subzone set for rent."),
    Economy_ParentRentSellFail("&cCannot sell a Residence if its parent zone is for rent."),
    Economy_SubzoneSellFail("&cCannot sell a subzone."),
    Economy_SellRentFail("&cCannot rent a Residence if it is for sale."),
    Economy_ParentSellRentFail("&cCannot rent a Residence if its parent zone is for sale."),
    Economy_OwnerBuyFail("&cCannot buy your own land!"),
    Economy_OwnerRentFail("&cCannot rent your own land!"),
    Economy_AlreadySellFail("&eResidence already for sale!"),
    Economy_LeaseRenew("&eLease valid until &6%1"),
    Economy_LeaseRenewMax("&eLease renewed to maximum allowed"),
    Economy_LeaseNotExpire("&eNo such lease, or lease does not expire."),
    Economy_LeaseRenewalCost("&eRenewal cost for area &6%1 &eis &6%2"),
    Economy_LeaseInfinite("&eLease time set to infinite..."),
    Economy_MarketDisabled("&cEconomy Disabled!"),
    Economy_SellAmount("&eSell Amount: &2%1"),
    Economy_SellList(" &6%1&e. &6%2 &e(&6%3&e) - &6%4"),
    Economy_LeaseExpire("&eLease Expire Time: &2%1"),
    Economy_LeaseList("&6%1. &e%2 &2%3 &e%4"),

    Expanding_North("&eExpanding North &6%1 &eblocks"),
    Expanding_West("&eExpanding West &6%1 &eblocks"),
    Expanding_South("&eExpanding South &6%1 &eblocks"),
    Expanding_East("&eExpanding East &6%1 &eblocks"),
    Expanding_Up("&eExpanding Up &6%1 &eblocks"),
    Expanding_Down("&eExpanding Down &6%1 &eblocks"),

    Contracting_North("&eContracting North &6%1 &eblocks"),
    Contracting_West("&eContracting West &6%1 &eblocks"),
    Contracting_South("&eContracting South &6%1 &eblocks"),
    Contracting_East("&eContracting East &6%1 &eblocks"),
    Contracting_Up("&eContracting Up &6%1 &eblocks"),
    Contracting_Down("&eContracting Down &6%1 &eblocks"),

    Shifting_North("&eShifting North &6%1 &eblocks"),
    Shifting_West("&eShifting West &6%1 &eblocks"),
    Shifting_South("&eShifting South &6%1 &eblocks"),
    Shifting_East("&eShifting East &6%1 &eblocks"),
    Shifting_Up("&eShifting Up &6%1 &eblocks"),
    Shifting_Down("&eShifting Down &6%1 &eblocks"),

    Limits_PGroup("&7- &ePermissions Group:&3 %1"),
    Limits_RGroup("&7- &eResidence Group:&3 %1"),
    Limits_Admin("&7- &eResidence Admin:&3 %1"),
    Limits_CanCreate("&7- &eCan Create Residences:&3 %1"),
    Limits_MaxRes("&7- &eMax Residences:&3 %1"),
    Limits_MaxEW("&7- &eMax East/West Size:&3 %1"),
    Limits_MaxNS("&7- &eMax North/South Size:&3 %1"),
    Limits_MaxUD("&7- &eMax Up/Down Size:&3 %1"),
    Limits_MinMax("&7- &eMin/Max Protection Height:&3 %1 to %2"),
    Limits_MaxSubzones("&7- &eMax Subzones:&3 %1"),
    Limits_MaxSubDepth("&7- &eMax Subzone Depth:&3 %1"),
    Limits_MaxRents("&7- &eMax Rents:&3 %1"),
    Limits_MaxRentDays(" &eMax Rent days:&3 %1"),
    Limits_EnterLeavePrefix(""),
    Limits_EnterLeave("&7- &eCan Set Enter/Leave Messages:&3 %1"),
    Limits_NumberOwn("&7- &eNumber of Residences you own:&3 %1"),
    Limits_Cost("&7- &eResidence Cost Per Block:&3 %1"),
    Limits_Sell("&7- &eResidence Sell Cost Per Block:&3 %1"),
    Limits_Flag("&7- &eFlag Permissions:&3 %1"),
    Limits_MaxDays("&7- &eMax Lease Days:&3 %1"),
    Limits_LeaseTime("&7- &eLease Time Given on Renew:&3 %1"),
    Limits_RenewCost("&7- &eRenew Cost Per Block:&3 %1"),

    Gui_Set_Title("&8%1 flags"),
    Gui_Pset_Title("&8%1 &7%2 &6flags"),
    Gui_Flag_NameColor("&7"),
    Gui_Actions(new ArrayList<String>(Arrays.asList("&2Left click to enable", "&cRight click to disable", "&eShift + left click to remove"))),

    InformationPage_Top("&e___/ &a %1 - %2 &e \\___"),
    InformationPage_TopSingle("&e___/ &a %1 &e \\___"),
    InformationPage_GeneralList("&2 %1 &6- &e%2"),
    InformationPage_FlagsList("&2 %1 &6- &e%2"),
    InformationPage_SmallSeparator("&6------"),

    Chat_ChatChannelChange("&eChanged residence chat channel to &6%1!"),
    Chat_ChatChannelLeave("&eLeft residence chat"),

    Chat_ChatMessage("%1 %2%3: %4%5"),
    Chat_ChatListeningMessage("&2[Listening %6]%1 %2%3: %4%5"),

    Chat_JoinFirst("&4Join residence chat channel first..."),
    Chat_InvalidChannel("&4Invalid Channel..."),
    Chat_InvalidColor("&4Incorrect color code"),
    Chat_NotInChannel("&4Player is not in channel"),
    Chat_Kicked("&6%1 &ewas kicked from &6%2 &echannel"),
    Chat_InvalidPrefixLength("&4Prefix is to long. Allowed length: %1"),
    Chat_ChangedColor("&eResidence chat channel color changed to %1"),
    Chat_ChangedPrefix("&eResidence chat channel prefix changed to %1"),

    Shop_ListTopLine("&6%1 &eShop list - Page &6%2 &eof &6%3 %4"),
    Shop_List(" &e%1. &6%2 &e(&6%3&e) %4"),
    Shop_ListVoted("&e%1 (&6%2&e)"),
    Shop_ListLiked("&7Likes: &7%1"),

    Shop_VotesTopLine("&6%1 &e%2 residence vote list &6- &ePage &6%3 &eof &6%4 %5"),
    Shop_VotesList(" &e%1. &6%2 &e%3 &7%4"),

    Shop_NoDesc("&6No description"),
    Shop_Desc("&6Description:\n%1"),
    Shop_DescChange("&6Description changed to: %1"),
    Shop_ChantChange("&4Can't change while shop flag is set to true"),
    Shop_NewBoard("&6Successfully added new shop sign board"),
    Shop_BoardExist("&cShop board already exists in this location"),
    Shop_DeleteBoard("&6Right click sign of board you want to delete"),
    Shop_DeletedBoard("&6Sign board removed"),
    Shop_IncorrectBoard("&cThis is not sign board, try performing command again and clicking correct sign"),
    Shop_InvalidSelection("&cLeft click with selection tool top left sign and then right click bottom right"),
    Shop_ToBigSelection("&cYour selection is too big, max allowed is 16 blocks"),
    Shop_ToDeapSelection("&cYour selection is too deap, max allowed is 16x16x1 blocks"),
    Shop_VoteChanged("&6Vote changed from &e%1 &6to &e%2 &6for &e%3 &6residence"),
    Shop_Voted("&6You voted, and gave &e%1 &6votes to &e%2 &6residence"),
    Shop_Liked("&6You liked &e%1 &6residence"),
    Shop_AlreadyLiked("&6You already liked &e%1 &6residence"),
    Shop_NoVotes("&cThere is no registered votes for this residence"),
    Shop_CantVote("&cResidence don't have shop flag set to true"),
    Shop_VotedRange("&6Vote range is from &e%1 &6to &e%2"),
    Shop_SignLines_1("&e--== &8%1 &e==--"),
    Shop_SignLines_2("&9%1"),
    Shop_SignLines_3("&4%1"),
    Shop_SignLines_4("&8%1&e (&8%2&e)"),
    Shop_SignLines_Likes4("&9Likes: &8%2"),

    RandomTeleport_TpLimit("&eYou can't teleport so fast, please wait &6%1 &esec and try again"),
    RandomTeleport_TeleportSuccess("&eTeleported to X:&6%1&e, Y:&6%2&e, Z:&6%3 &elocation"),
    RandomTeleport_IncorrectLocation("&6Could not find correct teleport location, please wait &e%1 &6sec and try again."),
    RandomTeleport_Disabled("&cRandom teleportation is disabled in this world"),
    RandomTeleport_TeleportStarted("&eTeleportation started, don't move for next &6%4 &esec."),
    RandomTeleport_WorldList("&ePossible worlds: &6%1"),

    Permissions_variableColor("&f"),
    Permissions_permissionColor("&6"),
    Permissions_cmdPermissionColor("&2"),

    General_DisabledWorld("&cResidence plugin is disabled in this world"),
    General_CantCreate("&cCan't create residences in this world"),
    General_UseNumbers("&cPlease use numbers..."),
    General_CantPlaceLava("&cYou can't place lava outside residence and higher than &6%1 &cblock level", "Replace all text with '' to disable this message"),
    General_CantPlaceWater("&cYou can't place Water outside residence and higher than &6%1 &cblock level", "Replace all text with '' to disable this message"),
    General_CantPlaceChest("&cYou can't place chest at this place"),
    General_NoPermission("&cYou don't have permission for this."),
    General_info_NoPlayerPermission("&c[playerName] doesn't have [permission] permission"),
    General_NoCmdPermission("&cYou don't have permission for this command."),
    General_DefaultUsage("&eType &6/%1 ? &efor more info"),
    General_MaterialGet("&eThe material name for ID &6%1 &eis &6%2"),
    General_MarketList("&e---- &6Market List &e----"),
    General_Separator("&e----------------------------------------------------"),
    General_AdminOnly("&cOnly admins have access to this command."),
    General_InfoTool("&e- Info Tool: &6%1"),
    General_ListMaterialAdd("&6%1 &eadded to the residence &6%2"),
    General_ListMaterialRemove("&6%1 &eremoved from the residence &6%2"),
    General_ItemBlacklisted("&cYou are blacklisted from using this item here."),
    General_WorldPVPDisabled("&cWorld PVP is disabled."),
    General_NoPVPZone("&cNo PVP zone."),
    General_NoFriendlyFire("&cNo friendly fire"),
    General_InvalidHelp("&cInvalid help page."),

    General_TeleportDeny("&cYou don't have teleport access."),
    General_TeleportSuccess("&eTeleported!"),
    General_TeleportConfirmBlock("&cThis teleport is not safe, you will fall into &6%1&c. Use &6/res tpconfirm &cto perform teleportation anyways."),
    General_TeleportConfirmVoid("&cThis teleport is not safe, you will fall into &6void&c. Use &6/res tpconfirm &cto perform teleportation anyways."),
    General_TeleportConfirm("&cThis teleport is not safe, you will fall for &6%1 &cblocks. Use &6/res tpconfirm &cto perform teleportation anyways."),
    General_TeleportStarted("&eTeleportation to &6%1 &estarted, don't move for next &6%2 &esec."),
    General_TeleportTitle("&eTeleporting!"),
    General_TeleportTitleTime("&6%1"),
    General_TeleportCanceled("&eTeleportation canceled!"),
    General_NoTeleportConfirm("&eThere is no teleports waiting for confirmation!"),
    General_HelpPageHeader2("&eHelp Pages - &6%1 &e- Page <&6%2 &eof &6%3&e>"),
    General_ListExists("&cList already exists..."),
    General_ListRemoved("&eList removed..."),
    General_ListCreate("&eCreated list &6%1"),
    General_PhysicalAreas("&ePhysical Areas"),

    General_CurrentArea("&eCurrent Area: &6%1"),
    General_TotalResSize("&eTotal size: &6%1m\u00B3 (%2m\u00B2)"),
    General_ResSize_eastWest("&eEast/West: &6%1"),
    General_ResSize_northSouth("&eNorth/South: &6%1"),
    General_ResSize_upDown("&eUp/Down: &6%1"),
    General_TotalWorth("&eTotal worth of residence: &6%1 &e(&6%2&e)"),
    General_TotalSubzones("&eSubzones in residence: &6%1 &e(&6%2&e)"),
    General_NotOnline("&eTarget player must be online."),

    General_GenericPages("&ePage &6%1 &eof &6%2 &e(&6%3&e)"),
    General_WorldEditNotFound("&cWorldEdit was not detected."),
    General_CoordsTop("&eX:&6%1 &eY:&6%2 &eZ:&6%3"),
    General_CoordsBottom("&eX:&6%1 &eY:&6%2 &eZ:&6%3"),
    General_CoordsLiner("&7 (&3%1&7;%2&7)"),
    General_AllowedTeleportIcon("&2T"),
    General_BlockedTeleportIcon("&7T"),
    General_AllowedMovementIcon("&2M"),
    General_BlockedMovementIcon("&7M"),
    General_AdminToggleTurnOn("&eAutomatic resadmin toggle turned &6On"),
    General_AdminToggleTurnOff("&eAutomatic resadmin toggle turned &6Off"),
    General_NoSpawn("&eYou do not have &6move &epermissions at your spawn point. Relocating"),
    General_CompassTargetReset("&eYour compass has been reset"),
    General_CompassTargetSet("&eYour compass now points to &6%1"),
    General_Ignorelist("&2Ignorelist:&6"),
    General_Blacklist("&cBlacklist:&6"),
    General_LandCost("&eLand cost: &6%1"),
    General_True("&2True"),
    General_False("&cFalse"),
    General_Removed("&6Removed"),
    General_FlagState("&eFlag state: %1"),
    General_Land("&eLand: &6%1"),
    General_Cost("&eCost: &6%1 &eper &6%2 &edays"),
    General_Status("&eStatus: %1"),
    General_Available("&2Available"),
    General_Size(" &eSize: &6%1"),
    General_ResidenceFlags("&eResidence flags: &6%1"),
    General_PlayersFlags("&ePlayers flags: &6%1"),
    General_GroupFlags("&eGroup flags: &6%1"),
    General_OthersFlags("&eOthers flags: &6%1"),
    General_Moved("&eMoved..."),
    General_Name("&eName: &6%1"),
    General_Lists("&eLists: &6"),
    General_Residences("&eResidences&6"),
    General_CreatedOn("&eCreated on: &6%1"),
    General_Owner("&eOwner: &6%1"),
    General_World("&eWorld: &6%1"),
    General_Subzones("&eSubzones"),
    General_NewPlayerInfo(
            "&eIf you want to create protected area for your house, please use wooden axe to select opposite sides of your home and execute command &2/res create YourResidenceName",
            "The below lines represent various messages residence sends to the players.",
            "Note that some messages have variables such as %1 that are inserted at runtime.");

    private Object text;
    private String[] comments;

    private lm(Object text, String... comments) {
        this.text = text;
        this.comments = comments;
    }

    public Object getText() {
        return text;
    }

    public String[] getComments() {
        return comments;
    }

    public String getPath() {
        String path = this.name();
        if (!this.name().contains("Language.") && !this.name().contains("CommandHelp."))
            path = "Language." + this.name();
        return path.replace("_", ".");
    }

    public String getMessage(Object... variables) {
        return Residence.getInstance().getLM().getMessage(this, variables);
    }

    public List<String> getMessageList() {
        return Residence.getInstance().getLM().getMessageList(this);
    }

    public void sendMessage(CommandSender sender, Object... variables) {

        if (sender == null)
            return;

        if (Residence.getInstance().getLM().containsKey(getPath())) {
            String msg = Residence.getInstance().getLM().getMessage(this, variables);

            if (msg.length() > 0) {

                List<String> MessageType = Residence.getInstance().getConfigManager().getMessageType();
                if (MessageType != null && MessageType.contains(this.name())) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(msg);
                        return;
                    }

                    Player player = (Player) sender;
                    if (player.hasMetadata("NPC")) {
                        return;
                    }

                    switch (Residence.getInstance().getConfigManager().getGeneralMessageType()) {
                    case ActionBar:
                        CMIActionBar.send(player, msg);
                        break;
                    case ChatBox:
                        player.sendMessage(msg);
                        break;
                    case TitleBar:
                        String title = msg;
                        String subtitle = "";
                        if (title.contains("\\n")) {
                            String[] parts = title.split("\\\\n", 2);
                            title = parts[0];
                            subtitle = parts.length > 1 ? parts[1] : "";
                        }
                        CMITitleMessage.send(player, title, subtitle);
                        break;
                    default:
                        break;
                    }
                    return;
                }
                // not target MessageType
                sender.sendMessage(msg);
                return;
            }
            // msg is empty
            return;
        }
        // not matching path in language, use default path msg
        String msg = getPath();
        if (msg.length() > 0) {
            sender.sendMessage(msg);
        }
    }

    public static String getMessageByPath(String path) {
        return Residence.getInstance().getLM().getMessage(path);
    }

    public static void showMessage(CommandSender sender, String text) {
        if (sender != null && text.length() > 0)
            sender.sendMessage(CMIChatColor.translate(text));
    }

    public static void consoleMessage(String message) {
        consoleMessage(message, true);
    }

    public static void consoleMessage(String message, boolean prefix) {
        Bukkit.getConsoleSender().sendMessage(CMIChatColor.translate((prefix ? Residence.getInstance().getPrefix() + " &f" : "") + message));
    }
}