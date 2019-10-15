package com.njits.iot.advert.baseinfo.basebean;

public class MdtlItem
{
    public String channelNo;
    public boolean delete = false;
    
    public Long esn;
    public Long gatewayId;
    public Long groupId;
    public String iccid;
    public Long id;
    public boolean inUse;
    public String mac;
    public Long mdtid;
    public String module;
    public Long oldMdtid;
    public String phoneNo;
    public Long protocalversion;
    public String remark;
    public String type;
    public String version;
    
    public String getChannelNo()
    {
        return channelNo;
    }
    
    public void setChannelNo(String channelNo)
    {
        this.channelNo = channelNo;
    }
    
    public String getIccid()
    {
        return iccid;
    }
    
    public void setIccid(String iccid)
    {
        this.iccid = iccid;
    }
    
    
    public String getMac()
    {
        return mac;
    }
    
    public void setMac(String mac)
    {
        this.mac = mac;
    }
    
    
    public String getModule()
    {
        return module;
    }
    
    public void setModule(String module)
    {
        this.module = module;
    }
    
    
    public String getPhoneNo()
    {
        return phoneNo;
    }
    
    public void setPhoneNo(String phoneNo)
    {
        this.phoneNo = phoneNo;
    }
    
    
    public String getRemark()
    {
        return remark;
    }
    
    public void setRemark(String remark)
    {
        this.remark = remark;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public boolean isDelete()
    {
        return delete;
    }
    
    public void setDelete(boolean delete)
    {
        this.delete = delete;
    }
    
    public Long getEsn()
    {
        return esn;
    }
    
    public void setEsn(Long esn)
    {
        this.esn = esn;
    }
    
    public Long getGatewayId()
    {
        return gatewayId;
    }
    
    public void setGatewayId(Long gatewayId)
    {
        this.gatewayId = gatewayId;
    }
    
    public Long getGroupId()
    {
        return groupId;
    }
    
    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public boolean isInUse()
    {
        return inUse;
    }
    
    public void setInUse(boolean inUse)
    {
        this.inUse = inUse;
    }
    
    public Long getMdtid()
    {
        return mdtid;
    }
    
    public void setMdtid(Long mdtid)
    {
        this.mdtid = mdtid;
    }
    
    public Long getOldMdtid()
    {
        return oldMdtid;
    }
    
    public void setOldMdtid(Long oldMdtid)
    {
        this.oldMdtid = oldMdtid;
    }
    
    public Long getProtocalversion()
    {
        return protocalversion;
    }
    
    public void setProtocalversion(Long protocalversion)
    {
        this.protocalversion = protocalversion;
    }
}
